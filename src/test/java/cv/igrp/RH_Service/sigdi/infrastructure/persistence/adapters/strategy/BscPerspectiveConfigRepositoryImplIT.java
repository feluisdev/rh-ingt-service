package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.strategy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.BscPerspectiveConfigMapper;
import java.util.ArrayList;
import java.util.List;
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
 * Exercises the REAL two-phase "parking" write in {@link BscPerspectiveConfigRepositoryImpl#saveAll}
 * against a genuine PostgreSQL instance (not H2), so the non-deferrable UNIQUE constraint on
 * display_order (V26) is enforced exactly as it is in production.
 * <p>
 * Regression coverage for 73-REVIEW.md WR-03: {@code UpdateBscPerspectivesCommandHandlerTest}
 * mocks {@code BscPerspectiveConfigRepository} entirely, so it never runs the actual parking
 * mechanism. Without this test, a regression that collapses the two-phase parking write back into
 * a single {@code saveAllAndFlush()} call -- or that breaks the parking-offset formula -- would
 * not be caught by any automated test; it would only surface as a live UNIQUE constraint
 * violation during a real swap. Postgres enforces UNIQUE constraints immediately per statement
 * (not deferred to commit), so a genuine swap attempted without the parking phase throws a
 * {@code DataIntegrityViolationException} -- that is exactly the failure mode this test guards
 * against via {@code assertDoesNotThrow}.
 * <p>
 * {@code *IT} naming (mirrors {@code OptionMigrationIT} in the {@code parametrizacoes} module)
 * deliberately excludes this test from default {@code mvn test} Surefire discovery (no
 * {@code Test}/{@code Tests}/{@code TestCase} suffix), since it requires a Docker daemon for
 * Testcontainers. Run explicitly via {@code mvn test -Dtest=BscPerspectiveConfigRepositoryImplIT}
 * in an environment with Docker available.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BscPerspectiveConfigRepositoryImpl.class, BscPerspectiveConfigMapper.class})
@Testcontainers
class BscPerspectiveConfigRepositoryImplIT {

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("rh_test")
          .withUsername("test")
          .withPassword("test");

  @Autowired
  private BscPerspectiveConfigRepositoryImpl repository;

  @Test
  void saveAllSwapsTwoRowsWithoutUniqueConstraintViolation() {
    // V26 Flyway-seeds exactly 4 rows on context startup -- read them back rather than
    // hand-constructing rows, mirroring how the real handler always operates on the
    // pre-existing, Flyway-seeded set (CONTEXT.md: "sem operação exposta de criar/apagar linha").
    List<BscPerspectiveConfig> seeded = repository.findAll();
    assertEquals(4, seeded.size(), "V26 seeds exactly 4 rows, one per fixed internal code");

    BscPerspectiveConfig first = seeded.stream()
        .filter(c -> c.getDisplayOrder().equals(1))
        .findFirst()
        .orElseThrow();
    BscPerspectiveConfig second = seeded.stream()
        .filter(c -> c.getDisplayOrder().equals(2))
        .findFirst()
        .orElseThrow();

    // Swap first <-> second's display_order; keep the other two rows exactly as seeded. The
    // production call shape always sends the complete 4-row set (UpdateBscPerspectivesCommandHandler
    // always builds an updated list for all 4 items), so this test mirrors that shape rather than
    // sending a partial 2-row list.
    List<BscPerspectiveConfig> toSave = new ArrayList<>(seeded.size());
    for (BscPerspectiveConfig config : seeded) {
      if (config.getId().equals(first.getId())) {
        toSave.add(config.update(config.getLabel(), second.getDisplayOrder()));
      } else if (config.getId().equals(second.getId())) {
        toSave.add(config.update(config.getLabel(), first.getDisplayOrder()));
      } else {
        toSave.add(config);
      }
    }

    // The assertion that matters most: this must NOT throw. A regression that removes the
    // parking phase (or breaks its offset formula) surfaces here as a
    // DataIntegrityViolationException from the non-deferrable UNIQUE constraint on display_order.
    assertDoesNotThrow(() -> repository.saveAll(toSave));

    List<BscPerspectiveConfig> after = repository.findAll();
    assertEquals(4, after.size(), "saveAll must not create or delete rows");

    BscPerspectiveConfig firstAfter =
        after.stream().filter(c -> c.getId().equals(first.getId())).findFirst().orElseThrow();
    BscPerspectiveConfig secondAfter =
        after.stream().filter(c -> c.getId().equals(second.getId())).findFirst().orElseThrow();

    assertEquals(second.getDisplayOrder(), firstAfter.getDisplayOrder(),
        "first row must now hold second's original position");
    assertEquals(first.getDisplayOrder(), secondAfter.getDisplayOrder(),
        "second row must now hold first's original position");
  }
}
