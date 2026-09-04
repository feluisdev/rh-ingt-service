package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
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
        Purpose purpose = (query.getPurpose() == null || query.getPurpose().isBlank())
                ? Purpose.PAA
                : Purpose.fromCodeOrThrow(query.getPurpose());

        // Optional year scoping (59-REVIEW.md WR-03): when the caller supplies a year, mirror
        // the year-scoped gate used by ContractualizeObjectivesCommandHandler; otherwise keep
        // the pre-existing year-agnostic, date-range-only lookup for unaffected callers.
        PaaSubmissionPeriod activePeriod = (query.getYear() == null
                ? repository.findActiveByTypeAndPurpose(type, purpose)
                : repository.findActiveByTypeAndYearAndPurpose(type, query.getYear(), purpose))
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
        response.setPurpose(activePeriod.getPurpose().getCode());
        response.setPurposeDesc(activePeriod.getPurpose().getDescription());

        long days = ChronoUnit.DAYS.between(LocalDate.now(AppTimeZone.CABO_VERDE), activePeriod.getEndDate());
        // NAV-03 (ACH-M-03): this used to be Math.max(0, days), which reported an expired
        // period as having exactly zero days left -- indistinguishable from one ending today.
        // The dashboard read that 0 and rendered "Hoje" in red for a deadline that had passed
        // three weeks earlier, and the frontend had no way to tell the two apart because the
        // information had already been destroyed here. A negative value is the honest answer
        // and is what deriveDeadlineNotifications' own `daysRemaining < 0` branch was written
        // to consume -- that branch had never once been reached.
        response.setDaysRemaining(days);

        return ResponseEntity.ok(response);
    }
}
