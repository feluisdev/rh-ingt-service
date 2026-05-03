package cv.igrp.RH_Service.parametrizacoes.infrastructure.migrations;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class OptionMigrationIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("rh_test")
        .withUsername("test")
        .withPassword("test");

    private Flyway buildFlyway() {
        return Flyway.configure()
            .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();
    }

    @Test
    void migracoes_devemSerIdempotentes() throws Exception {
        Flyway flyway = buildFlyway();
        flyway.migrate();

        long countFirst = countOptionEntities();

        flyway.migrate();

        long countSecond = countOptionEntities();
        assertEquals(countFirst, countSecond, "Segunda execução de migrate não deve alterar contagem de registos");
    }

    @Test
    void seedOptionEntity_deveTer11Grupos() throws Exception {
        buildFlyway().migrate();

        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var stmt = conn.prepareStatement(
                "SELECT COUNT(DISTINCT ccode) FROM t_option_entity WHERE active = true");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            int distinctCcodes = rs.getInt(1);
            assertEquals(11, distinctCcodes, "Deve existir seed para os 11 grupos de etiquetas");
        }
    }

    @Test
    void seedOptionEntity_deveConterMaritalStatus() throws Exception {
        buildFlyway().migrate();

        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM t_option_entity WHERE ccode = 'MARITAL_STATUS' AND locale = 'pt-CV'");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            assertTrue(rs.getInt(1) >= 5, "MARITAL_STATUS deve ter pelo menos 5 entradas");
        }
    }

    private long countOptionEntities() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM t_option_entity");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
