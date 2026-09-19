package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.SaldoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component("colabsAprovarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class AprovarPedidoAusenciaCommandHandler
        implements CommandHandler<AprovarPedidoAusenciaCommand, ResponseEntity<SuccessResponseDTO>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final SaldoAusenciaService saldoAusenciaService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<SuccessResponseDTO> handle(AprovarPedidoAusenciaCommand command) {
        var pedido = pedidoRepository.findById(PedidoAusenciaId.from(command.getPedidoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido não encontrado: " + command.getPedidoId()));

        pedido.aprovar(
                FuncionarioId.from(command.getRequest().getAprovadoPorId()),
                LocalDate.now(),
                command.getRequest().getObservacoesDecisao());

        // Os dias já estavam reservados desde a submissão: aprovar passa-os a gozados.
        saldoAusenciaService.confirmarGozo(pedido);

        pedidoRepository.save(pedido);
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getPedidoId()));
    }
}
