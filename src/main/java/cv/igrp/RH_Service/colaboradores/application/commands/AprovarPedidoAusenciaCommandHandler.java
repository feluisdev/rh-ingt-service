package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.DecisaoPedidoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Aprovação pelo RH: decide sempre. O pedido tem de ser do colaborador do caminho (senão 404). */
@Component("colabsAprovarPedidoAusenciaCommandHandler")
@RequiredArgsConstructor
public class AprovarPedidoAusenciaCommandHandler
        implements CommandHandler<AprovarPedidoAusenciaCommand, ResponseEntity<SuccessResponseDTO>> {

    private final DecisaoPedidoAusenciaService decisaoService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(AprovarPedidoAusenciaCommand command) {
        decisaoService.aprovar(null, FuncionarioId.from(command.getFuncionarioId()),
                PedidoAusenciaId.from(command.getPedidoId()),
                FuncionarioId.from(command.getRequest().getAprovadoPorId()),
                command.getRequest().getObservacoesDecisao());
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getPedidoId()));
    }
}
