package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCancelarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class CancelarPedidoAusenciaCommandHandler
        implements CommandHandler<CancelarPedidoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final SaldoAusenciaRepository saldoAusenciaRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CancelarPedidoAusenciaCommand command) {
        var pedido = pedidoRepository.findById(PedidoAusenciaId.from(command.getPedidoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido não encontrado: " + command.getPedidoId()));

        var solicitanteId = FuncionarioId.from(command.getSolicitanteId());
        if (!solicitanteId.equals(pedido.getFuncionarioId()))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.FORBIDDEN,
                    "Apenas o próprio funcionário pode cancelar o pedido.");

        String estadoAnterior = pedido.getEstado();
        pedido.cancelar();

        if (PedidoAusencia.APROVADO.equals(estadoAnterior)) {
            tipoAusenciaRepository.findById(pedido.getTipoAusenciaId()).ifPresent(tipo -> {
                if (Boolean.TRUE.equals(tipo.getDeductsBalance())) {
                    saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(
                            pedido.getFuncionarioId(),
                            pedido.getTipoAusenciaId(),
                            pedido.getDataInicio().getYear()
                    ).ifPresent(saldo -> {
                        saldo.decrementarPendentes(pedido.getNumeroDias());
                        saldoAusenciaRepository.save(saldo);
                    });
                }
            });
        }

        pedidoRepository.save(pedido);
        return ResponseEntity.ok(Map.of("message", "Cancelado com sucesso"));
    }
}
