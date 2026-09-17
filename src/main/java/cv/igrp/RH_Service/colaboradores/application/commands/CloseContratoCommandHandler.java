package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.EstadoColaboradorResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CessacaoService;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Encerramento do contrato = <b>cessação da relação de emprego público</b>. Delega no
 * {@link CessacaoService}, o mesmo caminho da mudança para um estado de cessação: cessa o
 * contrato, encerra a afectação (o Lugar fica vago), muda o estado do trabalhador e regista
 * o histórico. Antes, este caminho deixava o trabalhador ACTIVE, sem contrato e sem Lugar.
 *
 * <p>Não serve para renovar ou substituir um contrato: criar um contrato novo já encerra o
 * anterior (motivo {@code SUBSTITUICAO}) sem tocar na afectação nem no estado.
 */
@Component("colabsCloseContratoCommandHandler")
@RequiredArgsConstructor
public class CloseContratoCommandHandler
        implements CommandHandler<CloseContratoCommand, ResponseEntity<EstadoColaboradorResponseDTO>> {

    private final ContratoRepository contratoRepository;
    private final CessacaoService cessacaoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<EstadoColaboradorResponseDTO> handle(CloseContratoCommand command) {
        if (command.getEndDate() == null)
            throw IgrpResponseStatusException.badRequest("O campo endDate é obrigatório.");
        if (command.getTerminationReason() == null || command.getTerminationReason().isBlank())
            throw IgrpResponseStatusException.badRequest("O campo terminationReason é obrigatório.");

        var contrato = contratoRepository.findById(ContratoId.from(command.getContratoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Contrato não encontrado: " + command.getContratoId()));

        var funcionarioId = FuncionarioId.from(contrato.getFuncionarioId().getValor());
        var estadoCessacao = cessacaoService.estadoDeCessacaoPorOmissao();

        var cessacao = cessacaoService.cessar(funcionarioId, estadoCessacao, command.getEndDate(),
                command.getTerminationReason(), null);

        return ResponseEntity.ok(new EstadoColaboradorResponseDTO(
                funcionarioId.getStringValor(),
                texto(cessacao.estadoAnteriorId()),
                estadoCessacao.getId().getStringValor(),
                estadoCessacao.getCode(),
                command.getEndDate(),
                true,
                texto(cessacao.contratoCessadoId()),
                texto(cessacao.afectacaoEncerradaId())));
    }

    private static String texto(UUID valor) {
        return valor == null ? null : valor.toString();
    }
}
