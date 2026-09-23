package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.PedidoAusenciaMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Terminar antes do fim um pedido em horas aprovado — a amamentação que acaba mais cedo (V58). A
 * decisão fica; o período acaba na véspera. As férias continuam a suspender-se pelo art. 8.º.
 */
@Component
@RequiredArgsConstructor
public class TerminarPedidoAusenciaCommandHandler
        implements CommandHandler<TerminarPedidoAusenciaCommand, ResponseEntity<PedidoAusenciaResponseDTO>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final PedidoAusenciaMapper mapper;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<PedidoAusenciaResponseDTO> handle(TerminarPedidoAusenciaCommand command) {
        var pedido = pedidoRepository.findById(PedidoAusenciaId.from(command.getPedidoId()))
                .filter(p -> p.getFuncionarioId().equals(FuncionarioId.from(command.getFuncionarioId())))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido de ausência não encontrado: " + command.getPedidoId()));
        var dto = command.getRequest();
        pedido.terminar(dto != null ? dto.getData() : null, dto != null ? dto.getMotivo() : null);
        return ResponseEntity.ok(mapper.toDTO(pedidoRepository.save(pedido)));
    }
}
