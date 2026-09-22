package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AssignmentRepository {
    Assignment save(Assignment assignment);
    Optional<Assignment> findById(AssignmentId id);
    Optional<Assignment> findCurrentPrincipalByFuncionario(FuncionarioId funcionarioId);
    List<Assignment> findCurrentByFuncionario(FuncionarioId funcionarioId);
    List<Assignment> findAllByFuncionarioOrderByDataInicioDesc(FuncionarioId funcionarioId);
    /**
     * O titular corrente do Lugar, se o houver. Titular e' quem ocupa o Lugar a titulo
     * PRINCIPAL: um Lugar tem no maximo um, mas pode ter ao mesmo tempo quem la esteja em
     * substituicao ou em acumulacao -- por isso a pergunta e' pelo titular, e nao por
     * "afectacao corrente", que ja nao e' unica.
     */
    Optional<Assignment> findTitularByPosition(UUID positionId);

    /** O Lugar esta provido, isto e', tem titular corrente. Uma substituicao nao o provê. */
    boolean temTitular(UUID positionId);

    /**
     * Quem substitui, neste momento, o titular desta afectacao. No maximo um -- garantido
     * por indice unico parcial (V46).
     */
    Optional<Assignment> findSubstitutoCorrente(AssignmentId titularAssignmentId);

    /**
     * Todas as substituicoes correntes desta afectacao de titular. O indice garante que e'
     * uma so; a lista existe para o encerramento nao depender dessa garantia e conseguir
     * fechar o que encontrar.
     */
    List<Assignment> findSubstituicoesCorrentes(AssignmentId titularAssignmentId);

    /**
     * Substituições em que um colaborador está envolvido, nos <b>dois papéis</b>: como
     * substituto e como titular substituído. É o que faltava para um ecrã de RH conseguir
     * mostrar quem substitui quem — até aqui a substituição criava-se e nada a lia.
     *
     * @param apenasCorrentes verdadeiro devolve só as que estão em vigor; falso, o histórico
     */
    List<Assignment> findSubstituicoesDoFuncionario(FuncionarioId funcionarioId, boolean apenasCorrentes);

    /** As substituições correntes de um Lugar — a mesma pergunta, do lado do Lugar. */
    List<Assignment> findSubstituicoesCorrentesDoLugar(UUID positionId);

    /**
     * Quais dos {@code positionIds} tem titular. Existe para que uma listagem de Lugares
     * resolva o provimento numa consulta so, em vez de um {@link #temTitular} por linha.
     * Os ids ausentes do resultado estao vagos.
     */
    Set<UUID> findPositionIdsComTitular(Collection<UUID> positionIds);

    /**
     * Quem esteve afectado a um Lugar da unidade orgânica {@code unidadeOrganicaId} durante
     * o ano {@code year}, resolvido pelas datas da afectação (dataInicio/dataFim), não pelo
     * {@code isCurrent}.
     *
     * <p>Decisão herdada de {@code 116-CONTEXT.md}: o {@code isCurrent} responde a "onde está
     * hoje"; esta consulta responde a "onde estava no ano do período". São perguntas diferentes
     * -- uma afectação já encerrada (isCurrent = false) que cobriu o ano do período tem de ser
     * devolvida, e uma afectação actual (isCurrent = true) que só começou depois desse ano não.
     * Sem esta distinção, um período de 2027 planeado em 2029 devolveria a unidade errada.
     *
     * <p>A porta é year-shaped de propósito: quem a chama raciocina em anos; a tradução para
     * {@link java.time.LocalDate} é detalhe de infra-estrutura.
     *
     * <p>A unidade orgânica não vive na afectação: vive no Lugar ({@code Position.unidadeOrganicaId}).
     * A travessia Afectacao -> Lugar é feita na consulta, para não trazer N+1 a quem chama.
     */
    List<Assignment> findAllByUnidadeOrganicaCoveringYear(UUID unidadeOrganicaId, int year);
}
