package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaRequestDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pedido de ausência feito pelo próprio ({@code /me}).
 *
 * <p>Segue <b>as mesmas regras</b> do pedido lançado pelo RH — delega no
 * {@link CreatePedidoAusenciaCommandHandler}: contagem da linha do catálogo (art. 76.º, V56), feriados
 * do período (V55), os três tectos (V53), a opção do art. 43.º n.º 2 e o pedido em horas (V58). Antes
 * contava dias de calendário e não via nada disto. Aqui fica só o que é do self-service: quem pede é o
 * utilizador autenticado, e tem de estar activo.
 *
 * <p>Sem opção do art. 43.º n.º 2: pelo self-service ninguém classifica uma falta sua como
 * injustificada, e um tipo injustificado submetido por aqui é recusado pelo caminho comum (422).
 */
@Component("colabsSelfServiceCriarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class SelfServiceCriarPedidoAusenciaCommandHandler
        implements CommandHandler<SelfServiceCriarPedidoAusenciaCommand, ResponseEntity<SuccessResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final CreatePedidoAusenciaCommandHandler createPedidoAusenciaCommandHandler;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<SuccessResponseDTO> handle(SelfServiceCriarPedidoAusenciaCommand command) {
        var dto = command.getRequest();
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        if (dto.getEndDate().isBefore(dto.getStartDate()))
            throw IgrpResponseStatusException.badRequest("A data de fim não pode ser anterior à data de início.");

        var pedido = new PedidoAusenciaRequestDTO();
        pedido.setTipoAusenciaId(dto.getLeaveTypeId().toString());
        pedido.setDataInicio(dto.getStartDate());
        pedido.setDataFim(dto.getEndDate());
        pedido.setMotivo(dto.getNotes());
        pedido.setHoraInicio(dto.getStartTime());
        pedido.setHoraFim(dto.getEndTime());

        var criado = createPedidoAusenciaCommandHandler.handle(
                new CreatePedidoAusenciaCommand(funcionarioId.getStringValor(), pedido)).getBody();

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(criado.getId()));
    }
}
