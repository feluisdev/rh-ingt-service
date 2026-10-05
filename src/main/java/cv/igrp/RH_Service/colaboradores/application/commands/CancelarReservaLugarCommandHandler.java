package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.ReservaLugarService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** BR-AF-26: cancela a reserva; o Lugar fica livre para outros. */
@Component
@RequiredArgsConstructor
public class CancelarReservaLugarCommandHandler
        implements CommandHandler<CancelarReservaLugarCommand, ResponseEntity<SuccessResponseDTO>> {

    private final ReservaLugarService reservaLugarService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<SuccessResponseDTO> handle(CancelarReservaLugarCommand command) {
        String motivo = command.getRequest() != null ? command.getRequest().getMotivo() : null;
        var reserva = reservaLugarService.cancelar(FuncionarioId.from(command.getFuncionarioId()), motivo);
        return ResponseEntity.ok(SuccessResponseDTO.de(reserva.getId().getStringValor()));
    }
}
