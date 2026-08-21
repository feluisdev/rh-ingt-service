package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.lang.reflect.Method;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Guards the fix for ACH-C-02: fiscal_year was never written by any production command, so
 * every scenario simulation returned zero activities no matter how much real data existed.
 *
 * <p>These tests exercise the lifecycle callback directly instead of booting JPA. What they
 * can prove that way is the derivation rule itself; what they cannot prove is that Hibernate
 * invokes it. That second half is covered by asserting the annotations are present -- weaker
 * than a real persist, but the alternative here is an in-memory database whose dialect
 * differs from the PostgreSQL this schema targets.
 */
class TacticalActivitiesEntityFiscalYearTest {

  @Test
  void deriveFiscalYear_usesStartDateYear() {
    TacticalActivitiesEntity entity = new TacticalActivitiesEntity();
    entity.setStartDate(LocalDate.of(2026, 3, 14));
    entity.setEndDate(LocalDate.of(2026, 11, 30));

    entity.deriveFiscalYearFromStartDate();

    assertEquals(2026, entity.getFiscalYear());
  }

  @Test
  void deriveFiscalYear_followsStartDateWhenItMovesToAnotherYear() {
    // The value is recomputed, not filled in only when null: start_date is the single source
    // of truth. An activity moved into another year must move its fiscal year with it, or the
    // simulation keeps finding it under the year it no longer belongs to.
    TacticalActivitiesEntity entity = new TacticalActivitiesEntity();
    entity.setStartDate(LocalDate.of(2025, 1, 10));
    entity.deriveFiscalYearFromStartDate();
    assertEquals(2025, entity.getFiscalYear());

    entity.setStartDate(LocalDate.of(2027, 1, 10));
    entity.deriveFiscalYearFromStartDate();

    assertEquals(2027, entity.getFiscalYear());
  }

  @Test
  void deriveFiscalYear_leavesExistingValueAloneWhenStartDateIsNull() {
    // Nothing to derive from. Blanking a previously correct value would be worse than
    // leaving it: it would hide the activity from every simulation.
    TacticalActivitiesEntity entity = new TacticalActivitiesEntity();
    entity.setFiscalYear(2024);
    entity.setStartDate(null);

    entity.deriveFiscalYearFromStartDate();

    assertEquals(2024, entity.getFiscalYear());
  }

  @Test
  void deriveFiscalYear_staysNullWhenThereIsNothingToDeriveFrom() {
    TacticalActivitiesEntity entity = new TacticalActivitiesEntity();

    entity.deriveFiscalYearFromStartDate();

    assertNull(entity.getFiscalYear());
  }

  @Test
  void deriveFiscalYear_isWiredToBothJpaLifecycleEvents() throws NoSuchMethodException {
    // Without both annotations the derivation silently stops happening on one of the two
    // write paths, and nothing else in the suite would notice.
    Method callback = TacticalActivitiesEntity.class
        .getDeclaredMethod("deriveFiscalYearFromStartDate");

    assertNotNull(callback.getAnnotation(PrePersist.class), "@PrePersist is missing");
    assertNotNull(callback.getAnnotation(PreUpdate.class), "@PreUpdate is missing");
    assertTrue(java.lang.reflect.Modifier.isPublic(callback.getModifiers())
            || !java.lang.reflect.Modifier.isPrivate(callback.getModifiers()),
        "JPA callbacks must not be private");
  }
}
