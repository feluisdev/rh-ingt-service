package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaGradeDTO;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.GradeMapper;
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
public class GetGradesByCategoryIdQueryHandler
        implements QueryHandler<GetGradesByCategoryIdQuery, ResponseEntity<WrapperListaGradeDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetGradesByCategoryIdQueryHandler.class);

    private final CategoryRepository categoryRepository;
    private final GradeRepository gradeRepository;
    private final GradeMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaGradeDTO> handle(GetGradesByCategoryIdQuery query) {
        var categoryId = CategoryId.from(query.getCategoryId());

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Categoria não encontrada: " + query.getCategoryId()));

        var content = gradeRepository.findByCategoryIdOrderByGradeNumber(categoryId).stream()
                .map(mapper::toDTO)
                .toList();

        var wrapper = new WrapperListaGradeDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(content.size());

        return ResponseEntity.ok(wrapper);
    }
}
