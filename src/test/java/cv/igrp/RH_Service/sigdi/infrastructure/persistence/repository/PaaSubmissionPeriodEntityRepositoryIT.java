package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.PaaSubmissionPeriodMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical.PaaSubmissionPeriodRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Proves DATA-01's zone fix against a REAL PostgreSQL instance (not H2) whose session timezone
 * is confirmed NON-Cabo-Verde (UTC) -- the opposite of this dev machine's native PostgreSQL 18
 * install, which 79-RESEARCH.md's Pitfall 1 found is coincidentally pre-configured to
 * "Atlantic/Cape_Verde", making any live/manual test on that machine a false negative for this
 * exact bug class.
 * <p>
 * Before the 79-01 fix, {@code PaaSubmissionPeriodEntityRepository}'s active-period JPQL queries
 * used SQL {@code CURRENT_DATE}, evaluated against whatever timezone the Postgres SESSION
 * happened to be configured with -- not the application's Cabo Verde business rule. After the
 * fix ({@link PaaSubmissionPeriodRepositoryImpl}), "today" is computed once in Java via
 * {@code LocalDate.now(AppTimeZone.CABO_VERDE)} and bound as an explicit {@code :today}
 * parameter, so the session's own timezone setting becomes irrelevant to the result.
 * <p>
 * This test forces (and confirms, via {@code SHOW timezone}) the container's session zone to
 * UTC -- {@code postgres:17-alpine} already defaults to UTC with no override (confirmed live in
 * 79-RESEARCH.md; unlike this dev machine's native install), and the explicit {@code TZ} env var
 * below documents that intent rather than relying only on the image default -- then asserts
 * {@link PaaSubmissionPeriodRepositoryImpl#findActiveByTypeAndPurpose} still correctly finds a
 * period referenced against the Cabo Verde "today", proving the result no longer silently
 * depends on the database's own configured zone.
 * <p>
 * Note: reproducing the exact historical symptom (CURRENT_DATE returning the wrong calendar day
 * during the roughly one-hour daily window where UTC has rolled to the next day but Cabo Verde,
 * UTC-1, has not yet) would require freezing wall-clock time via an injectable {@code
 * java.time.Clock} -- explicitly out of scope for this bug-fix phase (79-RESEARCH.md
 * "Alternatives Considered": this codebase has zero existing Clock/DI-for-time usage, and
 * CONTEXT.md locked a plain {@code ZoneId} constant instead). This test instead proves the fix's
 * environment-independence directly: the query returns the correct row using the explicit
 * Cabo-Verde-computed "today", regardless of what zone the underlying Postgres session is
 * configured with.
 * <p>
 * {@code *IT} naming (mirrors {@code BscPerspectiveConfigRepositoryImplIT}) deliberately
 * excludes this test from default {@code mvn test} Surefire discovery, since it requires a
 * Docker daemon for Testcontainers. Run explicitly via
 * {@code mvn test -Dtest=PaaSubmissionPeriodEntityRepositoryIT} in an environment with Docker
 * available -- unreachable in this phase's own execution session (Docker Desktop daemon
 * unreachable, see 79-02-SUMMARY.md), matching the same fallback documented for the identical
 * gap in 79-RESEARCH.md Open Question 4.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PaaSubmissionPeriodRepositoryImpl.class, PaaSubmissionPeriodMapper.class})
@Testcontainers
class PaaSubmissionPeriodEntityRepositoryIT {

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("rh_test")
          .withUsername("test")
          .withPassword("test")
          .withEnv("TZ", "UTC");

  @Autowired
  private PaaSubmissionPeriodRepositoryImpl repository;

  @PersistenceContext
  private EntityManager entityManager;

  @Test
  void findActiveByTypeAndPurposeReturnsRowRegardlessOfNonCaboVerdeSessionZone() {
    // Confirm the container session really is non-Cabo-Verde (UTC) -- unlike this dev machine's
    // native Postgres (79-RESEARCH.md Pitfall 1) -- so the assertion below is meaningful proof,
    // not a coincidence of a Cabo-Verde-preconfigured session.
    Object sessionTimeZone = entityManager.createNativeQuery("SHOW timezone").getSingleResult();
    assertEquals("UTC", String.valueOf(sessionTimeZone));

    LocalDate todayCaboVerde = LocalDate.now(AppTimeZone.CABO_VERDE);
    PaaSubmissionPeriod period = PaaSubmissionPeriod.create(
        Purpose.PAA, PaaLevel.UNIT_LEVEL,
        todayCaboVerde.minusDays(2), todayCaboVerde.plusDays(2), todayCaboVerde.getYear());
    repository.save(period);

    Optional<PaaSubmissionPeriod> found =
        repository.findActiveByTypeAndPurpose(PaaLevel.UNIT_LEVEL, Purpose.PAA);

    assertTrue(found.isPresent(), "Active period must be found using the explicit Cabo Verde "
        + "'today' parameter, even though the Postgres session itself is UTC-configured");
    assertEquals(todayCaboVerde.minusDays(2), found.get().getStartDate());
    assertEquals(todayCaboVerde.plusDays(2), found.get().getEndDate());
  }
}
