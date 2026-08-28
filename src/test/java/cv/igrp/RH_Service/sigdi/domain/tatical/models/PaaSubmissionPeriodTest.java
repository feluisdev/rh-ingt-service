package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaaSubmissionPeriodTest {

    @Test
    void createWithSiadapPurposeExposesPurposeViaGetter() {
        LocalDate today = LocalDate.now();

        PaaSubmissionPeriod period = PaaSubmissionPeriod.create(
                Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, today, today.plusDays(10), 2026);

        assertEquals(Purpose.SIADAP, period.getPurpose());
    }

    @Test
    void closePreservesPurposeAndIsActiveTodayUnaffected() {
        // Cabo Verde zone, matching isActiveToday()'s own LocalDate.now(AppTimeZone.CABO_VERDE) --
        // a tight endDate=today boundary (not the original +-1 day buffer, which was wide enough
        // that it passed identically regardless of zone -- Cabo Verde is at most 1 day off from
        // any other zone). This narrower window only actually distinguishes the two zones during
        // the ~1-hour/day period where they disagree on calendar date (23:00-23:59 Cabo Verde
        // time); a full Clock-injection refactor of isActiveToday() would be needed for a
        // fully deterministic regression test, which is out of this fix's proportionate scope.
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);

        PaaSubmissionPeriod period = PaaSubmissionPeriod.create(
                Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, today.minusDays(1), today, 2026);

        assertTrue(period.isActiveToday());

        PaaSubmissionPeriod closed = period.close();

        assertEquals(Purpose.SIADAP, closed.getPurpose());
        assertTrue(closed.isClosed());
    }

    @Test
    void createRejectsNullPurpose() {
        LocalDate today = LocalDate.now();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PaaSubmissionPeriod.create(null, PaaLevel.UNIT_LEVEL, today, today.plusDays(10), 2026));

        assertEquals("purpose é obrigatório", exception.getMessage());
    }
}
