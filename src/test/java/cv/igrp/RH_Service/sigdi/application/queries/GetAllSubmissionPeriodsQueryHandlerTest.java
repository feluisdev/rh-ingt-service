package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperListPaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Regression coverage for DATA-01 (79-RESEARCH.md Pitfall 1): each list item's {@code
 * daysRemaining} must be computed with {@link AppTimeZone#CABO_VERDE}, not the JVM default
 * zone. Expected values are computed in-test using the exact same zone production uses, so the
 * assertions stay correct on any machine.
 */
@ExtendWith(MockitoExtension.class)
class GetAllSubmissionPeriodsQueryHandlerTest {

    @Mock
    private PaaSubmissionPeriodRepository repository;

    @InjectMocks
    private GetAllSubmissionPeriodsQueryHandler handler;

    @Test
    void handleComputesDaysRemainingForEachPeriodUsingCaboVerdeZone() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod first = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                today.minusDays(5), today.plusDays(7), "OPEN", today.getYear());
        PaaSubmissionPeriod second = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                today.minusDays(2), today.plusDays(20), "OPEN", today.getYear());

        when(repository.findAll(0, 10)).thenReturn(List.of(first, second));
        when(repository.countAll()).thenReturn(2L);

        GetAllSubmissionPeriodsQuery query = new GetAllSubmissionPeriodsQuery("0", "10", null);

        ResponseEntity<WrapperListPaaSubmissionPeriodDTO> response = handler.handle(query);

        List<PaaSubmissionPeriodResponseDTO> data = response.getBody().getData();
        assertEquals(2, data.size());
        assertEquals(7L, data.get(0).getDaysRemaining());
        assertEquals(20L, data.get(1).getDaysRemaining());
    }
}
