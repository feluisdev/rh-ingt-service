package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsAtivarContratoCommandHandler")
public class AtivarContratoCommandHandler
        implements CommandHandler<AtivarContratoCommand, ResponseEntity<Map<String, ?>>> {

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarContratoCommand command) {
        throw IgrpResponseStatusException.badRequest(
            "Operação não suportada. Para criar um novo contrato use POST /employees/{id}/contracts.");
    }
}
