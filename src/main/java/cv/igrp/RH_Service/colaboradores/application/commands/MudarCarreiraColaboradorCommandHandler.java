package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.MudancaCarreiraResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.VinculoLaboralService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Mudança de carreira: o colaborador passa a ocupar um Lugar vago de outra carreira.
 * Aqui valida-se quem pode mudar; a mecânica vive em {@link AssignmentService#mudarCarreira}.
 *
 * <p>Exige-se o mesmo vínculo que a promoção. A mudança de carreira é evolução na carreira em
 * sentido amplo — um vínculo que não permite progredir também não permite mudar de carreira —
 * e é isso que a distingue da transferência, que não depende do vínculo por só mudar de cadeira.
 *
 * <p><b>As habilitações não se verificam.</b> A carreira não tem hoje campo que diga o requisito
 * habilitacional, e as qualificações do colaborador não têm nível normalizado; inferi-lo do
 * código seria adivinhar configuração que não existe. Como o concurso na promoção, regista-se o
 * despacho e a referência do concurso, e a responsabilidade é do acto administrativo.
 */
@Component
@RequiredArgsConstructor
public class MudarCarreiraColaboradorCommandHandler
        implements CommandHandler<MudarCarreiraColaboradorCommand, ResponseEntity<MudancaCarreiraResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final VinculoLaboralService vinculoLaboralService;
    private final AssignmentService assignmentService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<MudancaCarreiraResponseDTO> handle(MudarCarreiraColaboradorCommand command) {
        var req = command.getRequest();
        if (req == null || req.getDataEfeito() == null)
            throw IgrpResponseStatusException.badRequest("A data de efeito (dataEfeito) é obrigatória.");
        if (req.getPositionId() == null || req.getPositionId().isBlank())
            throw IgrpResponseStatusException.badRequest("O Lugar de destino (positionId) é obrigatório.");

        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O colaborador não está activo — não é possível mudar de carreira.");

        VinculoLaboral vinculo = vinculoLaboralService.doFuncionario(funcionarioId);
        if (!vinculo.isEligibleForProgression())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O vínculo laboral '" + vinculo.getDescription() + "' não permite mudança de carreira.");

        var mudanca = assignmentService.mudarCarreira(
                funcionarioId,
                uuid(req.getPositionId(), "positionId"),
                uuid(req.getGradeId(), "gradeId"),
                uuid(req.getFunctionId(), "functionId"),
                req.getDataEfeito(),
                notas(req.getDespachoNumero(), req.getConcursoRef(), req.getObservacoes()));

        var nova = mudanca.afectacao();
        return ResponseEntity.status(201).body(new MudancaCarreiraResponseDTO(
                nova.getId().getStringValor(),
                funcionarioId.getStringValor(),
                mudanca.lugarAnterior().getId().getStringValor(),
                mudanca.lugarAnterior().getNumeroLugar(),
                mudanca.carreiraAnterior().getId().getStringValor(),
                mudanca.carreiraAnterior().getName(),
                mudanca.categoriaAnterior().getId().getStringValor(),
                mudanca.categoriaAnterior().getName(),
                mudanca.lugarNovo().getId().getStringValor(),
                mudanca.lugarNovo().getNumeroLugar(),
                texto(mudanca.lugarNovo().getUnidadeOrganicaId()),
                mudanca.carreiraNova().getId().getStringValor(),
                mudanca.carreiraNova().getName(),
                mudanca.categoriaNova().getId().getStringValor(),
                mudanca.categoriaNova().getName(),
                mudanca.escalao().getId().getStringValor(),
                mudanca.escalao().getName(),
                texto(nova.getFunctionId()),
                req.getDataEfeito()));
    }

    private static String texto(UUID valor) {
        return valor == null ? null : valor.toString();
    }

    private static UUID uuid(String valor, String campo) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.badRequest("O campo " + campo + " não é um UUID válido: " + valor);
        }
    }

    private static String notas(String despachoNumero, String concursoRef, String observacoes) {
        StringBuilder notas = new StringBuilder("Mudança de carreira");
        if (despachoNumero != null && !despachoNumero.isBlank())
            notas.append(" (despacho ").append(despachoNumero).append(")");
        if (concursoRef != null && !concursoRef.isBlank())
            notas.append(" [concurso ").append(concursoRef).append("]");
        if (observacoes != null && !observacoes.isBlank())
            notas.append(" — ").append(observacoes);
        return notas.toString();
    }
}
