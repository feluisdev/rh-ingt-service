package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component("colabsAprovarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class AprovarPedidoAusenciaCommandHandler
        implements CommandHandler<AprovarPedidoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final SaldoAusenciaRepository saldoAusenciaRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AprovarPedidoAusenciaCommand command) {
        var pedido = pedidoRepository.findById(PedidoAusenciaId.from(command.getPedidoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido não encontrado: " + command.getPedidoId()));

        var tipo = tipoAusenciaRepository.findById(pedido.getTipoAusenciaId())
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de ausência não encontrado."));

        pedido.aprovar(
                FuncionarioId.from(command.getRequest().getAprovadoPorId()),
                LocalDate.now(),
                command.getRequest().getObservacoesDecisao());

        if (Boolean.TRUE.equals(tipo.getDeductsBalance())) {
            var saldo = saldoAusenciaRepository
                    .findByFuncionarioIdAndTipoAusenciaIdAndAno(
                            pedido.getFuncionarioId(),
                            pedido.getTipoAusenciaId(),
                            pedido.getDataInicio().getYear())
                    .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Não existe saldo de ausência para o funcionário, tipo e ano do pedido."));

            if (saldo.saldoDisponivel() < pedido.getNumeroDias())
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Saldo insuficiente. Disponível: " + saldo.saldoDisponivel() + ", necessário: " + pedido.getNumeroDias());

            saldo.incrementarPendentes(pedido.getNumeroDias());
            saldoAusenciaRepository.save(saldo);
        }

        pedidoRepository.save(pedido);
        return ResponseEntity.ok(Map.of("message", "Aprovado com sucesso"));
    }
}
