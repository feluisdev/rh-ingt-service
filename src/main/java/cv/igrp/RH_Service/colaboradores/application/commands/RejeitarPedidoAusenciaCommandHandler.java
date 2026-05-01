package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component("colabsRejeitarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class RejeitarPedidoAusenciaCommandHandler
        implements CommandHandler<RejeitarPedidoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final PedidoAusenciaRepository pedidoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(RejeitarPedidoAusenciaCommand command) {
        var pedido = pedidoRepository.findById(PedidoAusenciaId.from(command.getPedidoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido não encontrado: " + command.getPedidoId()));

        pedido.rejeitar(
                FuncionarioId.from(command.getRequest().getAprovadoPorId()),
                LocalDate.now(),
                command.getRequest().getObservacoesDecisao());

        pedidoRepository.save(pedido);
        return ResponseEntity.ok(Map.of("message", "Rejeitado com sucesso"));
    }
}
