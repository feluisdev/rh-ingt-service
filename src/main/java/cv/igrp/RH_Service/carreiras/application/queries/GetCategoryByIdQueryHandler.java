package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.application.dto.CategoryResponse;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CategoryMapper;
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
public class GetCategoryByIdQueryHandler
        implements QueryHandler<GetCategoryByIdQuery, ResponseEntity<CategoryResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCategoryByIdQueryHandler.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<CategoryResponse> handle(GetCategoryByIdQuery query) {
        var category = categoryRepository.findById(CategoryId.from(query.getCategoryId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Categoria não encontrada: " + query.getCategoryId()));

        return ResponseEntity.ok(mapper.toDTO(category));
    }
}
