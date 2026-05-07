package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.application.dto.GradeResponseDTO;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.GradeMapper;
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
public class GetGradeByIdQueryHandler
        implements QueryHandler<GetGradeByIdQuery, ResponseEntity<GradeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetGradeByIdQueryHandler.class);

    private final GradeRepository gradeRepository;
    private final GradeMapper mapper;
    private final CategoryRepository categoryRepository;
    private final CareerRepository careerRepository;

    @IgrpQueryHandler
    public ResponseEntity<GradeResponseDTO> handle(GetGradeByIdQuery query) {
        var grade = gradeRepository.findById(GradeId.from(query.getGradeId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Escalão não encontrado: " + query.getGradeId()));

        var cat = categoryRepository.findById(grade.getCategoryId());
        String categoryName = cat.map(c -> c.getName()).orElse(null);
        String careerName = cat.flatMap(c -> careerRepository.findById(c.getCareerId()))
                .map(cr -> cr.getName()).orElse(null);
        var dto = mapper.toDTO(grade, categoryName);
        dto.setCareerName(careerName);
        return ResponseEntity.ok(dto);
    }
}
