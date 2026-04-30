package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaCategoryDTO;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CategoryMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class GetCategoriesByCareerIdQueryHandler
        implements QueryHandler<GetCategoriesByCareerIdQuery, ResponseEntity<WrapperListaCategoryDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCategoriesByCareerIdQueryHandler.class);

    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaCategoryDTO> handle(GetCategoriesByCareerIdQuery query) {
        var careerId = CareerId.from(query.getCareerId());

        careerRepository.findById(careerId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Carreira não encontrada: " + query.getCareerId()));

        var content = categoryRepository.findByCareerId(careerId).stream()
                .map(mapper::toDTO)
                .toList();

        var wrapper = new WrapperListaCategoryDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(content.size());

        return ResponseEntity.ok(wrapper);
    }
}
