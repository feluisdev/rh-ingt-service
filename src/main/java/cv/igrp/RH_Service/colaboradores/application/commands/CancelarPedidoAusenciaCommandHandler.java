package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.SaldoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Component("colabsCancelarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class CancelarPedidoAusenciaCommandHandler
        implements CommandHandler<CancelarPedidoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final SaldoAusenciaService saldoAusenciaService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(CancelarPedidoAusenciaCommand command) {
        var pedido = pedidoRepository.findById(PedidoAusenciaId.from(command.getPedidoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido não encontrado: " + command.getPedidoId()));

        // Este é o caminho do RH: quem pode chegar aqui pode cancelar o pedido de
        // qualquer colaborador. O caminho do próprio é o self-service, que só deixa
        // cancelar o que é seu e ainda está pendente. Antes, esta verificação dava
        // 403 ao RH e ninguém conseguia cancelar um pedido alheio.
        var solicitanteId = FuncionarioId.from(command.getSolicitanteId());

        boolean estavaAprovado = pedido.getEstado() != null && pedido.getEstado().isAprovado();
        pedido.cancelar(solicitanteId, LocalDate.now(), null);

        // Aprovado devolve dias gozados; pendente liberta a reserva.
        if (estavaAprovado) saldoAusenciaService.devolverGozo(pedido);
        else saldoAusenciaService.libertarReserva(pedido);

        pedidoRepository.save(pedido);
        return ResponseEntity.ok(Map.of("message", "Cancelado com sucesso"));
    }
}
