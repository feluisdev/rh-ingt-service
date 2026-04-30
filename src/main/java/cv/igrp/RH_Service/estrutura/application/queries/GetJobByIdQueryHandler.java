package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.JobResponse;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.JobMapper;
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
public class GetJobByIdQueryHandler
        implements QueryHandler<GetJobByIdQuery, ResponseEntity<JobResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetJobByIdQueryHandler.class);

    private final JobRepository jobRepository;
    private final JobMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<JobResponse> handle(GetJobByIdQuery query) {
        var job = jobRepository.findById(JobId.from(query.getJobId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Cargo não encontrado: " + query.getJobId()));

        return ResponseEntity.ok(mapper.toDTO(job));
    }
}
