package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        LocalDate today = LocalDate.now();

        PaaSubmissionPeriod period = PaaSubmissionPeriod.create(
                Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, today.minusDays(1), today.plusDays(1), 2026);

        assertTrue(period.isActiveToday());

        PaaSubmissionPeriod closed = period.close();

        assertEquals(Purpose.SIADAP, closed.getPurpose());
        assertTrue(closed.isClosed());
    }

    @Test
    void backwardCompatibleCreateOverloadDefaultsPurposeToPaa() {
        LocalDate today = LocalDate.now();

        PaaSubmissionPeriod period = PaaSubmissionPeriod.create(
                PaaLevel.UNIT_LEVEL, today, today.plusDays(10), 2026);

        assertEquals(Purpose.PAA, period.getPurpose());
    }
}
