package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Regression coverage for DATA-01 (79-RESEARCH.md Pitfall 1): the {@code daysRemaining}
 * computation must use {@link AppTimeZone#CABO_VERDE}, not the JVM default zone. Tests compute
 * the expected value using the exact same zone production uses ({@code
 * LocalDate.now(AppTimeZone.CABO_VERDE)}), so the assertion stays correct on any machine --
 * including one whose native Postgres/OS clock is coincidentally already Cabo-Verde-configured.
 */
@ExtendWith(MockitoExtension.class)
class GetActiveSubmissionPeriodQueryHandlerTest {

    @Mock
    private PaaSubmissionPeriodRepository repository;

    @InjectMocks
    private GetActiveSubmissionPeriodQueryHandler handler;

    @Test
    void handleReturnsDaysRemainingComputedFromCaboVerdeZone() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        LocalDate startDate = today.minusDays(5);
        LocalDate endDate = today.plusDays(10);
        PaaSubmissionPeriod period = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                startDate, endDate, "OPEN", today.getYear());

        when(repository.findActiveByTypeAndPurpose(PaaLevel.UNIT_LEVEL, Purpose.PAA))
                .thenReturn(Optional.of(period));

        GetActiveSubmissionPeriodQuery query =
                new GetActiveSubmissionPeriodQuery(PaaLevel.UNIT_LEVEL.getCode(), Purpose.PAA.getCode(), null);

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response = handler.handle(query);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(10L, response.getBody().getDaysRemaining());
    }

    @Test
    void handleThrowsNotFoundWhenNoActivePeriodExists() {
        when(repository.findActiveByTypeAndPurpose(PaaLevel.UNIT_LEVEL, Purpose.PAA))
                .thenReturn(Optional.empty());

        GetActiveSubmissionPeriodQuery query =
                new GetActiveSubmissionPeriodQuery(PaaLevel.UNIT_LEVEL.getCode(), Purpose.PAA.getCode(), null);

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(query));

        assertEquals(404, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains(PaaLevel.UNIT_LEVEL.getDescription()));
    }
}
