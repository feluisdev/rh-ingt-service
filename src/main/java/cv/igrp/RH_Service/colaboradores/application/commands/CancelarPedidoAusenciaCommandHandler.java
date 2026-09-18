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

        // O pedido tem de pertencer ao colaborador do URL — caso contrário não existe
        // neste recurso. Antes comparava-se com o "solicitante" e devolvia-se 403, o
        // que impedia o RH de cancelar seja o que for: o controlador passa sempre o
        // funcionário do URL como solicitante, logo a regra dizia outra coisa do que
        // parecia. Quem pode cancelar decide-se por permissão, não por identidade: o
        // caminho do próprio é o self-service, que só deixa cancelar o que é seu e
        // ainda está pendente.
        var funcionarioDoUrl = FuncionarioId.from(command.getFuncionarioId());
        if (!funcionarioDoUrl.equals(pedido.getFuncionarioId()))
            throw IgrpResponseStatusException.notFound(
                    "Pedido de ausência não encontrado: " + command.getPedidoId());

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
