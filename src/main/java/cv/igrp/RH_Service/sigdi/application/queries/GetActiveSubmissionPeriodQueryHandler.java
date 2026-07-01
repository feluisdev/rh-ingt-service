package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class GetActiveSubmissionPeriodQueryHandler implements QueryHandler<GetActiveSubmissionPeriodQuery, ResponseEntity<PaaSubmissionPeriodResponseDTO>> {

    private final PaaSubmissionPeriodRepository repository;

    public GetActiveSubmissionPeriodQueryHandler(PaaSubmissionPeriodRepository repository) {
        this.repository = repository;
    }

    @IgrpQueryHandler
    @Override
    public ResponseEntity<PaaSubmissionPeriodResponseDTO> handle(GetActiveSubmissionPeriodQuery query) {
        PaaLevel type = PaaLevel.fromCodeOrThrow(query.getType());

        PaaSubmissionPeriod activePeriod = repository.findActiveByType(type)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Nenhum período de submissão ativo encontrado para " + type.getDescription()));

        PaaSubmissionPeriodResponseDTO response = new PaaSubmissionPeriodResponseDTO();
        response.setId(activePeriod.getId());
        response.setType(activePeriod.getType().getCode());
        response.setTypeDesc(activePeriod.getType().getDescription());
        response.setStartDate(activePeriod.getStartDate());
        response.setEndDate(activePeriod.getEndDate());
        response.setStatus(activePeriod.getStatus());
        response.setStatusDesc("Aberto");
        response.setYear(activePeriod.getYear());

        long days = ChronoUnit.DAYS.between(LocalDate.now(), activePeriod.getEndDate());
        response.setDaysRemaining(Math.max(0, days));

        return ResponseEntity.ok(response);
    }
}
