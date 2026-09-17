package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PromocaoResponseDTO;
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
 * Promoção do colaborador para a categoria imediatamente superior da mesma carreira.
 * Aqui valida-se quem pode ser promovido (colaborador activo, vínculo elegível); a mecânica
 * da afectação e da grelha vive em {@link AssignmentService#promover}.
 *
 * <p>A forma da promoção <b>infere-se do pedido</b>: com {@code positionId} a pessoa muda de
 * Lugar; sem ele, o Lugar actual sobe de categoria. Não é um campo do pedido nem se persiste —
 * deduz-se do histórico comparando os Lugares das afectações.
 */
@Component
@RequiredArgsConstructor
public class PromoverColaboradorCommandHandler
        implements CommandHandler<PromoverColaboradorCommand, ResponseEntity<PromocaoResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final VinculoLaboralService vinculoLaboralService;
    private final AssignmentService assignmentService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<PromocaoResponseDTO> handle(PromoverColaboradorCommand command) {
        var req = command.getRequest();
        if (req == null || req.getDataEfeito() == null)
            throw IgrpResponseStatusException.badRequest("A data de efeito (dataEfeito) é obrigatória.");
        if (req.getCategoryId() == null || req.getCategoryId().isBlank())
            throw IgrpResponseStatusException.badRequest("A categoria de destino (categoryId) é obrigatória.");

        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O colaborador não está activo — não é possível promover.");

        VinculoLaboral vinculo = vinculoLaboralService.doFuncionario(funcionarioId);
        if (!vinculo.isEligibleForProgression())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O vínculo laboral '" + vinculo.getDescription() + "' não permite promoção.");

        var promocao = assignmentService.promover(
                funcionarioId,
                uuid(req.getCategoryId(), "categoryId"),
                uuid(req.getPositionId(), "positionId"),
                uuid(req.getGradeId(), "gradeId"),
                req.getDataEfeito(),
                notas(req.getDespachoNumero(), req.getConcursoRef(), req.getObservacoes()));

        return ResponseEntity.status(201).body(new PromocaoResponseDTO(
                promocao.afectacao().getId().getStringValor(),
                funcionarioId.getStringValor(),
                promocao.afectacao().getPositionId().toString(),
                promocao.categoriaAnterior().getId().getStringValor(),
                promocao.categoriaAnterior().getName(),
                promocao.categoriaNova().getId().getStringValor(),
                promocao.categoriaNova().getName(),
                promocao.escalao().getId().getStringValor(),
                promocao.escalao().getName(),
                promocao.lugarReclassificado(),
                req.getDataEfeito()));
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
        StringBuilder notas = new StringBuilder("Promoção");
        if (despachoNumero != null && !despachoNumero.isBlank())
            notas.append(" (despacho ").append(despachoNumero).append(")");
        if (concursoRef != null && !concursoRef.isBlank())
            notas.append(" [concurso ").append(concursoRef).append("]");
        if (observacoes != null && !observacoes.isBlank())
            notas.append(" — ").append(observacoes);
        return notas.toString();
    }
}
