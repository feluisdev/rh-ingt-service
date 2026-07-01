package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;

@Component
public class ClosePaaSubmissionPeriodCommandHandler implements CommandHandler<ClosePaaSubmissionPeriodCommand, ResponseEntity<PaaSubmissionPeriodResponseDTO>> {

    private final PaaSubmissionPeriodRepository repository;

    public ClosePaaSubmissionPeriodCommandHandler(PaaSubmissionPeriodRepository repository) {
        this.repository = repository;
    }

    @IgrpCommandHandler
    @Transactional
    @Override
    public ResponseEntity<PaaSubmissionPeriodResponseDTO> handle(ClosePaaSubmissionPeriodCommand command) {
        PaaSubmissionPeriod period = repository.findById(command.getId())
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Período de submissão não encontrado"));

        PaaSubmissionPeriod closed = period.close();
        PaaSubmissionPeriod saved = repository.save(closed);

        PaaSubmissionPeriodResponseDTO response = new PaaSubmissionPeriodResponseDTO();
        response.setId(saved.getId());
        response.setType(saved.getType().getCode());
        response.setTypeDesc(saved.getType().getDescription());
        response.setStartDate(saved.getStartDate());
        response.setEndDate(saved.getEndDate());
        response.setStatus(saved.getStatus());
        response.setStatusDesc("Fechado");
        response.setYear(saved.getYear());
        response.setDaysRemaining(0L);

        return ResponseEntity.ok(response);
    }
}
