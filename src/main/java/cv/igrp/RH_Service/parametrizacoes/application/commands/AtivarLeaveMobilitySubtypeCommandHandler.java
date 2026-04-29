package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveMobilitySubtype;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveMobilitySubtypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AtivarLeaveMobilitySubtypeCommandHandler implements CommandHandler<AtivarLeaveMobilitySubtypeCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivarLeaveMobilitySubtypeCommandHandler.class);

    private final LeaveMobilitySubtypeRepository leaveMobilitySubtypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarLeaveMobilitySubtypeCommand command) {
        var id = ExternalID.from(java.util.UUID.fromString(command.getLeaveMobilitySubtypeId()));

        LeaveMobilitySubtype leaveMobilitySubtype = leaveMobilitySubtypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Subtipo de licença/mobilidade não encontrado: " + command.getLeaveMobilitySubtypeId()));

        leaveMobilitySubtype.reativar();
        leaveMobilitySubtypeRepository.save(leaveMobilitySubtype);

        return ResponseEntity.ok(Map.of("message", "Subtipo de licença/mobilidade activado com sucesso"));
    }
}
