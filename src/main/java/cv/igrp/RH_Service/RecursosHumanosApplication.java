package cv.igrp.RH_Service;

import cv.igrp.RH_Service.shared.config.ApplicationAuditorAware;
import cv.igrp.RH_Service.shared.config.AppTimeZone;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAware", dateTimeProviderRef = "auditDateTimeProvider")
@EnableCaching
@EnableScheduling
public class RecursosHumanosApplication {

  private static final Logger log = LoggerFactory.getLogger(RecursosHumanosApplication.class);

  @Bean
  public AuditorAware<String> auditAware() {
    return new ApplicationAuditorAware();
  }

  @Bean
  public DateTimeProvider auditDateTimeProvider() {
    // Cabo Verde zone, not the JVM default -- @CreatedDate/@LastModifiedDate on every
    // AuditEntity-derived entity must use the same explicit zone as AppTimeZone.CABO_VERDE
    // (see Phase 79 / DATA-01/DATA-03), or containers defaulting to UTC would silently
    // shift audit timestamps by the zone offset.
    return () -> Optional.of(LocalDateTime.now(AppTimeZone.CABO_VERDE));
  }


  public static void main(String[] args) {
    SpringApplication.run(RecursosHumanosApplication.class, args);
  }
}
