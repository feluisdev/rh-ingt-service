package cv.igrp.RH_Service.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuração customizada do Flyway para permitir limpeza da base de dados no arranque.
 * Útil para ambientes de desenvolvimento no Kubernetes onde o acesso direto à BD é restrito.
 */
@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.clean-on-startup:false}")
    private boolean cleanOnStartup;

    @Bean
    @Profile("development")
    public FlywayMigrationStrategy cleanMigrationStrategy() {
        return flyway -> {
            if (cleanOnStartup) {
                flyway.clean();
            }
            flyway.migrate();
        };
    }
}
