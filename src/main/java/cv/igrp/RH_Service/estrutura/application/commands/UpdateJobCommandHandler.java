package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.JobResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.JobMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateJobCommandHandler
        implements CommandHandler<UpdateJobCommand, ResponseEntity<JobResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateJobCommandHandler.class);

    private final JobRepository jobRepository;
    private final JobMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<JobResponseDTO> handle(UpdateJobCommand command) {
        var id = JobId.from(command.getJobId());
        var dto = command.getRequest();

        var job = jobRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Cargo não encontrado: " + command.getJobId()));

        if (jobRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe um cargo com code='" + dto.getCode() + "'.");
        }

        job.atualizar(dto.getCode(), dto.getName(), dto.getDescription());
        var updated = jobRepository.save(job);

        return ResponseEntity.ok(mapper.toDTO(updated));
    }
}
