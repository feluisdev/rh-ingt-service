package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DesativarCareerCommandHandler
        implements CommandHandler<DesativarCareerCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarCareerCommandHandler.class);

    private final CareerRepository careerRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DesativarCareerCommand command) {
        var id = CareerId.from(command.getCareerId());

        var career = careerRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Carreira não encontrada: " + command.getCareerId()));

        if (careerRepository.existsActiveCategoriesByCareerId(id)) {
            throw IgrpResponseStatusException.conflict(
                    "Não é possível desactivar a carreira: tem categorias activas.");
        }

        career.desativar();
        careerRepository.save(career);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getCareerId()));
    }
}
