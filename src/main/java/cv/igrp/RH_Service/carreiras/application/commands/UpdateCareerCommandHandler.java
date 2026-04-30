package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.application.dto.CareerResponse;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CareerMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateCareerCommandHandler
        implements CommandHandler<UpdateCareerCommand, ResponseEntity<CareerResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCareerCommandHandler.class);

    private final CareerRepository careerRepository;
    private final CareerMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<CareerResponse> handle(UpdateCareerCommand command) {
        var id = CareerId.from(command.getCareerId());
        var dto = command.getRequest();

        var career = careerRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Carreira não encontrada: " + command.getCareerId()));

        if (careerRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe uma carreira com code='" + dto.getCode() + "'.");
        }

        career.atualizar(dto.getCode(), dto.getName(), dto.getDescription());
        var updated = careerRepository.save(career);

        return ResponseEntity.ok(mapper.toDTO(updated));
    }
}
