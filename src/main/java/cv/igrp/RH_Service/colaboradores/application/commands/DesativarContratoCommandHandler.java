package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;

@Component("colabsDesativarContratoCommandHandler")
public class DesativarContratoCommandHandler
        implements CommandHandler<DesativarContratoCommand, ResponseEntity<SuccessResponseDTO>> {

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DesativarContratoCommand command) {
        throw IgrpResponseStatusException.badRequest(
            "Operação não suportada. Para encerrar um contrato use PUT /employees/{id}/contracts/{id}/close.");
    }
}
