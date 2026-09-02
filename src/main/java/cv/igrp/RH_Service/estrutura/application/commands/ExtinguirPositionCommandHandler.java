package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
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
public class ExtinguirPositionCommandHandler
        implements CommandHandler<ExtinguirPositionCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtinguirPositionCommandHandler.class);

    private final PositionRepository positionRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(ExtinguirPositionCommand command) {
        Position position = positionRepository.findById(PositionId.from(command.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + command.getPositionId()));

        position.extinguir();
        positionRepository.save(position);

        return ResponseEntity.ok(Map.of("message", "Lugar extinto com sucesso"));
    }
}
