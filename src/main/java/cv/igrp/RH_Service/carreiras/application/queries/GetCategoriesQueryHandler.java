package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaCategoryDTO;
import cv.igrp.RH_Service.carreiras.domain.filter.CategoryFilter;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CategoryMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetCategoriesQueryHandler
        implements QueryHandler<GetCategoriesQuery, ResponseEntity<WrapperListaCategoryDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCategoriesQueryHandler.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaCategoryDTO> handle(GetCategoriesQuery query) {
        var filter = new CategoryFilter();
        if (query.getCareerId() != null) {
            filter.setCareerId(UUID.fromString(query.getCareerId()));
        }
        filter.setIsActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var content = categoryRepository.findAll(filter).stream()
                .map(mapper::toDTO)
                .toList();

        var wrapper = new WrapperListaCategoryDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(content.size());

        return ResponseEntity.ok(wrapper);
    }
}
