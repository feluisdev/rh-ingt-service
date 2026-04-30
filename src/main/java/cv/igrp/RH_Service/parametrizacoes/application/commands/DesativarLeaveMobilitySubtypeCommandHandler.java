package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveMobilitySubtype;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveMobilitySubtypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveMobilitySubtypeId;
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
public class DesativarLeaveMobilitySubtypeCommandHandler implements CommandHandler<DesativarLeaveMobilitySubtypeCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarLeaveMobilitySubtypeCommandHandler.class);

    private final LeaveMobilitySubtypeRepository leaveMobilitySubtypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarLeaveMobilitySubtypeCommand command) {
        var id = LeaveMobilitySubtypeId.from(java.util.UUID.fromString(command.getLeaveMobilitySubtypeId()));

        LeaveMobilitySubtype leaveMobilitySubtype = leaveMobilitySubtypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Subtipo de licença/mobilidade não encontrado: " + command.getLeaveMobilitySubtypeId()));

        leaveMobilitySubtype.desativar();
        leaveMobilitySubtypeRepository.save(leaveMobilitySubtype);

        return ResponseEntity.ok(Map.of("message", "Subtipo de licença/mobilidade desactivado com sucesso"));
    }
}
