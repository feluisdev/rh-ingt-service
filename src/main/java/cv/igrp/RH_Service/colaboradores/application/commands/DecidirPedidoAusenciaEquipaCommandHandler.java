package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.DecisaoPedidoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Quem decide é o utilizador autenticado, e tem de ser a chefia directa de quem pediu. */
@Component
@RequiredArgsConstructor
public class DecidirPedidoAusenciaEquipaCommandHandler
        implements CommandHandler<DecidirPedidoAusenciaEquipaCommand, ResponseEntity<SuccessResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final DecisaoPedidoAusenciaService decisaoService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DecidirPedidoAusenciaEquipaCommand command) {
        var chefe = currentEmployeeResolver.resolve();
        var id = PedidoAusenciaId.from(command.getPedidoId());
        String motivo = command.getRequest() != null ? command.getRequest().getMotivo() : null;
        var p = command.isAprovar() ? decisaoService.aprovar(chefe, null, id, null, motivo)
                : decisaoService.rejeitar(chefe, null, id, null, motivo);
        return ResponseEntity.ok(SuccessResponseDTO.de(p.getId().getStringValor()));
    }
}
