package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
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
public class AtivarCareerCommandHandler
        implements CommandHandler<AtivarCareerCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivarCareerCommandHandler.class);

    private final CareerRepository careerRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarCareerCommand command) {
        var id = CareerId.from(command.getCareerId());

        var career = careerRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Carreira não encontrada: " + command.getCareerId()));

        career.reativar();
        careerRepository.save(career);

        return ResponseEntity.ok(Map.of("message", "Activado com sucesso"));
    }
}
