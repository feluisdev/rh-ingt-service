package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component("colabsSelfServiceCriarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class SelfServiceCriarPedidoAusenciaCommandHandler
        implements CommandHandler<SelfServiceCriarPedidoAusenciaCommand, ResponseEntity<Map<String, ?>>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final PedidoAusenciaRepository pedidoAusenciaRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(SelfServiceCriarPedidoAusenciaCommand command) {
        var dto = command.getRequest();
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var startDate = dto.getStartDate();
        var endDate = dto.getEndDate();

        if (endDate.isBefore(startDate))
            throw IgrpResponseStatusException.badRequest("A data de fim não pode ser anterior à data de início.");

        if (pedidoAusenciaRepository.existsOverlapForFuncionario(funcionarioId, startDate, endDate))
            throw IgrpResponseStatusException.badRequest(
                    "Já existe um pedido de ausência para o período indicado.");

        int numeroDias = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        var tipoAusenciaId = TipoAusenciaId.from(dto.getLeaveTypeId());

        var pedido = PedidoAusencia.criar(funcionarioId, tipoAusenciaId, startDate, endDate,
                numeroDias, dto.getNotes());
        var saved = pedidoAusenciaRepository.save(pedido);

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Pedido de ausência submetido com sucesso"));
    }
}
