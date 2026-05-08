package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsDesativarContratoCommandHandler")
public class DesativarContratoCommandHandler
        implements CommandHandler<DesativarContratoCommand, ResponseEntity<Map<String, ?>>> {

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarContratoCommand command) {
        throw IgrpResponseStatusException.badRequest(
            "Operação não suportada. Para encerrar um contrato use PUT /employees/{id}/contracts/{id}/close.");
    }
}
