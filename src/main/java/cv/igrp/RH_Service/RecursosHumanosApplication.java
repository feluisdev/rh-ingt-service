package cv.igrp.RH_Service;

import cv.igrp.RH_Service.shared.config.ApplicationAuditorAware;
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

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAware", dateTimeProviderRef = "auditDateTimeProvider")
@EnableCaching
public class RecursosHumanosApplication {

  private static final Logger log = LoggerFactory.getLogger(RecursosHumanosApplication.class);

  @Bean
  public AuditorAware<String> auditAware() {
    return new ApplicationAuditorAware();
  }

  @Bean
  public DateTimeProvider auditDateTimeProvider() {
    return () -> Optional.of(LocalDateTime.now());
  }

  public static void main(String[] args) {
    SpringApplication.run(RecursosHumanosApplication.class, args);
  }
}
