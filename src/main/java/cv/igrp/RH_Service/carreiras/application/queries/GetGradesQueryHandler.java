package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaGradeDTO;
import cv.igrp.RH_Service.carreiras.domain.filter.GradeFilter;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.GradeMapper;
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
public class GetGradesQueryHandler
        implements QueryHandler<GetGradesQuery, ResponseEntity<WrapperListaGradeDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetGradesQueryHandler.class);

    private final GradeRepository gradeRepository;
    private final GradeMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaGradeDTO> handle(GetGradesQuery query) {
        var filter = new GradeFilter();
        if (query.getCategoryId() != null) {
            filter.setCategoryId(UUID.fromString(query.getCategoryId()));
        }
        filter.setIsActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var content = gradeRepository.findAll(filter).stream()
                .map(mapper::toDTO)
                .toList();

        var wrapper = new WrapperListaGradeDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(content.size());

        return ResponseEntity.ok(wrapper);
    }
}
