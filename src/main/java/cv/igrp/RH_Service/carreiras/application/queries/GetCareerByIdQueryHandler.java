package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.application.dto.CareerResponse;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CareerMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCareerByIdQueryHandler
        implements QueryHandler<GetCareerByIdQuery, ResponseEntity<CareerResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCareerByIdQueryHandler.class);

    private final CareerRepository careerRepository;
    private final CareerMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<CareerResponse> handle(GetCareerByIdQuery query) {
        var career = careerRepository.findById(CareerId.from(query.getCareerId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Carreira não encontrada: " + query.getCareerId()));

        return ResponseEntity.ok(mapper.toDTO(career));
    }
}
