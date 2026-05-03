package cv.igrp.RH_Service.colaboradores.application.commands;

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

import java.util.Map;

@Component("colabsSelfServiceCancelarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class SelfServiceCancelarPedidoAusenciaCommandHandler
        implements CommandHandler<SelfServiceCancelarPedidoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final PedidoAusenciaRepository pedidoAusenciaRepository;

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

        if (!"PENDENTE".equals(pedido.getEstado()))
            throw IgrpResponseStatusException.badRequest(
                    "Apenas pedidos em estado PENDENTE podem ser cancelados.");

        pedido.cancelar();
        pedidoAusenciaRepository.save(pedido);

        return ResponseEntity.ok(Map.of("message", "Pedido de ausência cancelado com sucesso"));
    }
}
