package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.TransferenciaResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
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
 * Transferência do colaborador para outro Lugar, mantendo a posição na grelha.
 * Aqui valida-se quem pode ser transferido; a mecânica vive em
 * {@link AssignmentService#transferir}.
 *
 * <p>Ao contrário da progressão e da promoção, a transferência <b>não depende do vínculo</b>:
 * não há evolução na carreira, só mudança de cadeira.
 */
@Component
@RequiredArgsConstructor
public class TransferirColaboradorCommandHandler
        implements CommandHandler<TransferirColaboradorCommand, ResponseEntity<TransferenciaResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final AssignmentService assignmentService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<TransferenciaResponseDTO> handle(TransferirColaboradorCommand command) {
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
                    "O colaborador não está activo — não é possível transferir.");

        var transferencia = assignmentService.transferir(
                funcionarioId,
                uuid(req.getPositionId(), "positionId"),
                uuid(req.getFunctionId(), "functionId"),
                req.getDataEfeito(),
                notas(req.getDespachoNumero(), req.getObservacoes()));

        var nova = transferencia.afectacao();
        return ResponseEntity.status(201).body(new TransferenciaResponseDTO(
                nova.getId().getStringValor(),
                funcionarioId.getStringValor(),
                transferencia.lugarAnterior().getId().getStringValor(),
                transferencia.lugarAnterior().getNumeroLugar(),
                texto(transferencia.lugarAnterior().getUnidadeOrganicaId()),
                transferencia.lugarNovo().getId().getStringValor(),
                transferencia.lugarNovo().getNumeroLugar(),
                texto(transferencia.lugarNovo().getUnidadeOrganicaId()),
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

    private static String notas(String despachoNumero, String observacoes) {
        String notas = "Transferência" + (despachoNumero != null && !despachoNumero.isBlank()
                ? " (despacho " + despachoNumero + ")" : "");
        return observacoes != null && !observacoes.isBlank() ? notas + " — " + observacoes : notas;
    }
}
