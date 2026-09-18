package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.SaldoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Component("colabsSelfServiceCancelarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class SelfServiceCancelarPedidoAusenciaCommandHandler
        implements CommandHandler<SelfServiceCancelarPedidoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final PedidoAusenciaRepository pedidoAusenciaRepository;
    private final SaldoAusenciaService saldoAusenciaService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(SelfServiceCancelarPedidoAusenciaCommand command) {
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var pedido = pedidoAusenciaRepository.findById(PedidoAusenciaId.from(command.getPedidoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Pedido de ausência não encontrado: " + command.getPedidoId()));

        if (!pedido.getFuncionarioId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound("Pedido de ausência não encontrado: " + command.getPedidoId());

        if (pedido.getEstado() == null || !pedido.getEstado().isPendente())
            throw IgrpResponseStatusException.badRequest(
                    "Apenas pedidos em estado PENDENTE podem ser cancelados.");

        pedido.cancelar(funcionarioId, LocalDate.now(), null);

        // Retirado antes da decisão: os dias reservados voltam ao saldo.
        saldoAusenciaService.libertarReserva(pedido);

        pedidoAusenciaRepository.save(pedido);

        return ResponseEntity.ok(Map.of("message", "Pedido de ausência cancelado com sucesso"));
    }
}
