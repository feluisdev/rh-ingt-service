package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluatorSkipReason;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolve quem avalia um colaborador elegível, segundo as três regras travadas pelo operador
 * a 2026-08-27 (119-CONTEXT.md):
 * <ol>
 *   <li>Regra geral: o avaliador é o {@code responsibleEmployeeId} da unidade orgânica do
 *       colaborador.</li>
 *   <li>Quando esse responsável é o próprio colaborador (acontece sempre que ele dirija a
 *       unidade em que está enquadrado), sobe-se um único nível: o avaliador passa a ser o
 *       {@code responsibleEmployeeId} da unidade-mãe.</li>
 *   <li>A unidade de topo não tem unidade-mãe. Quem a dirige é saltado -- não recebe
 *       avaliação nesta fase -- com o motivo {@link EvaluatorSkipReason#TOP_UNIT_HEAD}. O
 *       avaliador da direcção (permissão {@code siadap.avaliacao.avaliarDirecao}) foi
 *       separado para milestone próprio por decisão do operador: {@code
 *       IgrpAuthorizationService.checkPermission} é <em>caller-scoped</em> e num agendador
 *       não há chamador.</li>
 * </ol>
 * A subida à unidade-mãe acontece no máximo uma vez: se o responsável da unidade-mãe for
 * também o próprio colaborador, o resultado é saltado com
 * {@link EvaluatorSkipReason#PARENT_UNIT_RESPONSIBLE_IS_SELF} -- não há regra 2 encadeada.
 * <p>
 * Este resolvedor não consulta enquadramentos (D-09, 119-02-PLAN.md): a unidade recebida já
 * vem decidida pela Fase 116, que aplicou o predicado temporal do ano do período.
 * Reconsultar aqui abriria uma segunda fonte de verdade sobre a mesma pergunta.
 * <p>
 * <b>Existe uma segunda derivação de avaliador no repositório</b>,
 * {@code CreateSiadapEvaluationCommandHandler.deriveEvaluatorId}, e esta classe não a
 * substitui nem é chamada por ela. São deliberadamente duas derivações separadas:
 * {@code deriveEvaluatorId} deriva a unidade por
 * {@code findCurrentOrganizationalUnitId} -- "onde o colaborador está hoje" -- e ignora
 * deliberadamente o {@code evaluatorId} do pedido (T-109-13); serve o caminho manual, chamado
 * a partir do {@code ComplianceController}. Este {@code EvaluatorResolver} raciocina em "onde
 * o colaborador esteve no ano do período" -- a pergunta que a Fase 116 responde -- e sobe à
 * unidade-mãe quando o responsável é o próprio, coisa que {@code deriveEvaluatorId} não faz
 * nem tem por onde fazer, porque nem sequer conhece o {@code parentUnitId}. Uma não chama a
 * outra porque respondem a perguntas diferentes sobre o mesmo domínio; fazer uma delegar na
 * outra confundiria "unidade actual" com "unidade do ano do período".
 */
@Component
@RequiredArgsConstructor
public class EvaluatorResolver {

    private final OrganicaLookupPort organicaLookupPort;

    public EvaluatorResolution resolve(UUID employeeId, UUID unitId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("employeeId não pode ser nulo");
        }
        if (unitId == null) {
            throw new IllegalArgumentException("unitId não pode ser nulo");
        }

        Optional<UUID> responsibleId = organicaLookupPort.findResponsibleEmployeeId(unitId);
        if (responsibleId.isEmpty()) {
            return EvaluatorResolution.skip(EvaluatorSkipReason.UNIT_WITHOUT_RESPONSIBLE);
        }

        if (!responsibleId.get().equals(employeeId)) {
            return EvaluatorResolution.of(responsibleId.get());
        }

        // O responsável da unidade é o próprio colaborador elegível: sobe-se à unidade-mãe,
        // uma única vez.
        Optional<UUID> parentUnitId = organicaLookupPort.findParentUnitId(unitId);
        if (parentUnitId.isEmpty()) {
            return EvaluatorResolution.skip(EvaluatorSkipReason.TOP_UNIT_HEAD);
        }

        Optional<UUID> parentResponsibleId = organicaLookupPort.findResponsibleEmployeeId(parentUnitId.get());
        if (parentResponsibleId.isEmpty()) {
            return EvaluatorResolution.skip(EvaluatorSkipReason.PARENT_UNIT_WITHOUT_RESPONSIBLE);
        }

        if (parentResponsibleId.get().equals(employeeId)) {
            return EvaluatorResolution.skip(EvaluatorSkipReason.PARENT_UNIT_RESPONSIBLE_IS_SELF);
        }

        return EvaluatorResolution.of(parentResponsibleId.get());
    }

    /**
     * Resultado de resolver o avaliador de um colaborador: exactamente um de
     * {@code evaluatorId} ou {@code skipReason}, nunca ambos e nunca nenhum (D-08,
     * 119-02-PLAN.md).
     */
    public record EvaluatorResolution(UUID evaluatorId, EvaluatorSkipReason skipReason) {

        public static EvaluatorResolution of(UUID evaluatorId) {
            return new EvaluatorResolution(evaluatorId, null);
        }

        public static EvaluatorResolution skip(EvaluatorSkipReason skipReason) {
            return new EvaluatorResolution(null, skipReason);
        }

        public boolean isSkipped() {
            return skipReason != null;
        }
    }
}
