package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaCategoryDTO;
import cv.igrp.RH_Service.carreiras.domain.filter.CategoryFilter;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CategoryMapper;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
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
    private final CareerRepository careerRepository;
    private final GradeRepository gradeRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaCategoryDTO> handle(GetCategoriesQuery query) {
        var filter = new CategoryFilter();
        if (query.getCareerId() != null) {
            filter.setCareerId(UUID.fromString(query.getCareerId()));
        }
        filter.setIsActive(query.getActive());
        filter.setCode(query.getCode());
        filter.setNome(query.getNome());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var pageResult = categoryRepository.findAll(filter);
        var content = pageResult.getData().stream().map(category -> {
            String careerName = careerRepository.findById(category.getCareerId())
                    .map(c -> c.getName()).orElse(null);
            var dto = mapper.toDTO(category, careerName);
            dto.setNEscaloes(gradeRepository.countByCategoryId(category.getId()));
            return dto;
        }).toList();

        var wrapper = new WrapperListaCategoryDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(pageResult.getTotalElements());
        wrapper.setPageNumber(pageResult.getPageNumber());
        wrapper.setPageSize(pageResult.getPageSize());
        wrapper.setTotalPages(pageResult.getTotalPages());
        wrapper.setFirst(pageResult.isFirst());
        wrapper.setLast(pageResult.isLast());

        return ResponseEntity.ok(wrapper);
    }
}
