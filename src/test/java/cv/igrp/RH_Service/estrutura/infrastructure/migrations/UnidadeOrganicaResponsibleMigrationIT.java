package cv.igrp.RH_Service.estrutura.infrastructure.migrations;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves criterion 4 of {@code ROADMAP.md § Phase 109}: the {@code responsible_employee_id}
 * column, and the Envers shadow that carries it, must appear on
 * {@code t_unidade_organica} in an environment where Hibernate's {@code ddl-auto} never
 * ran -- a genuinely empty PostgreSQL container reached only through Flyway
 * ({@code V32__unidade_organica_responsible_employee.sql}). This is the definition of
 * "clean environment" this plan uses: no prior schema, no {@code ddl-auto}, only Flyway.
 *
 * <p>This class matches {@code *IT.java} and is intentionally excluded from {@code mvn test}
 * -- {@code pom.xml} configures neither surefire nor failsafe for the {@code *IT} suffix.
 * Run it explicitly:
 * <pre>mvn -o test -Dtest=UnidadeOrganicaResponsibleMigrationIT</pre>
 *
 * <p>{@code classpath:db/migration} includes {@code V24__update_kpi_criteria_fix.sql} for
 * as long as that file stays on disk (it is present, untracked, and applied to the
 * operator's database, but was never committed -- see {@code 109-RESEARCH.md}). Plan 06 of
 * this phase measures the database under both conditions: with and without {@code V24} on
 * the classpath.
 */
@Testcontainers
class UnidadeOrganicaResponsibleMigrationIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("rh_test")
        .withUsername("test")
        .withPassword("test");

    /**
     * Dedicated, never-migrated container for the negative proof (case 4). Kept separate
     * from {@code postgres} above so that JUnit 5's unspecified test-method execution order
     * can never let an earlier test's {@code migrate()} create the table before this proof
     * runs against it.
     */
    @Container
    static final PostgreSQLContainer<?> postgresEmpty = new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("rh_test_empty")
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
    void colunaResponsavel_existeNaTabelaPrincipal_apposUmaPassagemDoFlyway() throws Exception {
        buildFlyway().migrate();

        assertEquals(1, countColumn("public", "t_unidade_organica", "responsible_employee_id"),
            "responsible_employee_id deve existir em public.t_unidade_organica depois de uma so passagem do Flyway");
    }

    @Test
    void sombraEnvers_existeSoEmAuditSchema_naoEmPublic() throws Exception {
        buildFlyway().migrate();

        assertEquals(1, countColumn("audit_schema", "t_unidade_organica_aud", "responsible_employee_id"),
            "responsible_employee_id deve existir em audit_schema.t_unidade_organica_aud");
        assertEquals(0, countColumn("public", "t_unidade_organica_aud", "responsible_employee_id"),
            "t_unidade_organica_aud nao deve ser criada em public -- D-07 nao hedgeia o CREATE");
    }

    @Test
    void migracoes_devemSerIdempotentes() throws Exception {
        Flyway flyway = buildFlyway();
        flyway.migrate();

        long countFirst = countColumn("public", "t_unidade_organica", "responsible_employee_id");

        flyway.migrate();

        long countSecond = countColumn("public", "t_unidade_organica", "responsible_employee_id");
        assertEquals(countFirst, countSecond,
            "Segunda execucao de migrate nao deve alterar a contagem de colunas de t_unidade_organica");
    }

    @Test
    void alterPuro_falhaContraContentorVazio_ondeCreateIfNotExistsPassaria() {
        SQLException thrown = assertThrows(SQLException.class, () -> {
            try (Connection conn = DriverManager.getConnection(
                    postgresEmpty.getJdbcUrl(), postgresEmpty.getUsername(), postgresEmpty.getPassword());
                 Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE t_unidade_organica ADD COLUMN responsible_employee_id UUID");
            }
        }, "Um ALTER puro sobre uma tabela inexistente tem de lancar SQLException -- e a prova negativa de D-02");

        assertTrue(thrown.getMessage().contains("t_unidade_organica"),
            "A mensagem da excecao deve nomear a tabela inexistente: " + thrown.getMessage());
    }

    private long countColumn(String schema, String table, String column) throws Exception {
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.columns "
                    + "WHERE table_schema = ? AND table_name = ? AND column_name = ?")) {
            stmt.setString(1, schema);
            stmt.setString(2, table);
            stmt.setString(3, column);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
