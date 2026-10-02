package cv.igrp.RH_Service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the full application context, which needs the real environment: a reachable PostgreSQL
 * configured through {@code POSTGRES_*} (Flyway runs on startup) and the rest of the variables in
 * {@code .env.example}. Without them the datasource URL stays as the literal
 * {@code jdbc:postgresql://${POSTGRES_HOST}:...} and the context cannot load -- an environment
 * gap, not a code defect.
 * <p>
 * {@code *IT} naming (as in {@code PaaSubmissionPeriodEntityRepositoryIT}) keeps it out of the
 * default {@code mvn test} Surefire run. Run it explicitly with the environment loaded, ideally
 * against a disposable copy of the database, since startup applies any pending migrations:
 * {@code mvn test -Dtest=RecursosHumanosApplicationIT}.
 */
@SpringBootTest
public class RecursosHumanosApplicationIT {

  @Test
  public void contextLoads() {

  }
}
