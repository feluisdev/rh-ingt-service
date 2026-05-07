package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaCareerDTO;
import cv.igrp.RH_Service.carreiras.domain.filter.CareerFilter;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CareerMapper;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
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
public class GetCareersQueryHandler
        implements QueryHandler<GetCareersQuery, ResponseEntity<WrapperListaCareerDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCareersQueryHandler.class);

    private final CareerRepository careerRepository;
    private final CareerMapper mapper;
    private final CategoryRepository categoryRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaCareerDTO> handle(GetCareersQuery query) {
        var filter = new CareerFilter();
        filter.setIsActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var pageResult = careerRepository.findAll(filter);
        var content = pageResult.getData().stream().map(career -> {
            var dto = mapper.toDTO(career);
            dto.setNCategorias(categoryRepository.countByCareerId(career.getId()));
            return dto;
        }).toList();

        var wrapper = new WrapperListaCareerDTO();
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
