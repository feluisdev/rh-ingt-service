package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaJobDTO;
import cv.igrp.RH_Service.estrutura.domain.filter.JobFilter;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.JobMapper;
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
public class GetJobsQueryHandler
        implements QueryHandler<GetJobsQuery, ResponseEntity<WrapperListaJobDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetJobsQueryHandler.class);

    private final JobRepository jobRepository;
    private final JobMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaJobDTO> handle(GetJobsQuery query) {
        var filter = new JobFilter();
        filter.setIsActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var content = jobRepository.findAll(filter).stream()
                .map(mapper::toDTO)
                .toList();

        var wrapper = new WrapperListaJobDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(content.size());

        return ResponseEntity.ok(wrapper);
    }
}
