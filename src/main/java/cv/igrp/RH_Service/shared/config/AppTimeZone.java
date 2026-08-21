package cv.igrp.RH_Service.shared.config;

import java.time.ZoneId;

/**
 * Single source of truth for the application's business-rule "today".
 * Cabo Verde is UTC-1 year-round (no DST) — never rely on the ambient
 * Postgres session timezone (CURRENT_DATE) or the JVM default zone
 * (LocalDate.now()) for date computations that decide period open/closed
 * state or days-remaining counts.
 */
public final class AppTimeZone {

    public static final ZoneId CABO_VERDE = ZoneId.of("Atlantic/Cape_Verde");

    private AppTimeZone() {}
}
