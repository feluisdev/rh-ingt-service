package cv.igrp.RH_Service.shared.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guarda contra a armadilha do {@code @Lob}: seed ou migration que escreva por SQL cru numa coluna
 * que o Hibernate mapeia como objeto grande do PostgreSQL corrompe a linha de forma que só se
 * manifesta na leitura — e para todas as leituras da tabela, não só a do campo afetado.
 *
 * <p>Incidente de origem: 4884af69 semeou {@code t_institutional_identity} por {@code INSERT} e o
 * percurso BSC inteiro passou a devolver 400; revertido por 5e819bfb. A guarda existe porque o
 * incidente está fechado mas a armadilha não: há {@value #KNOWN_TABLE_COUNT} tabelas com colunas
 * {@code @Lob}, entre elas {@code t_strategic_goals}.
 *
 * <p>Os três testes desta classe têm papéis distintos e nenhum deles é dispensável:
 * <ol>
 *   <li>o inventário prova que o detetor <em>vê</em> as colunas — sem isto, um detetor com o
 *       inventário vazio passa a verde sobre qualquer ficheiro;</li>
 *   <li>o caso vermelho prova que o detetor <em>reprova</em> o defeito real, corrido contra a cópia
 *       literal do {@code INSERT} de 4884af69;</li>
 *   <li>o caso verde prova que o estado atual do repositório está limpo.</li>
 * </ol>
 */
class LobColumnSqlGuardTest {

    private static final int KNOWN_TABLE_COUNT = 11;

    private static final Path REPOSITORY_ROOT = Path.of(".");
    private static final Path JAVA_SOURCE_ROOT = Path.of("src", "main", "java");
    private static final Path REGRESSION_FIXTURE =
            Path.of("src", "test", "resources", "lobguard", "regression-4884af69-institutional-identity.sql");

    /**
     * Inventário esperado, derivado do código a 2026-09-01 por
     * {@code rg -n "@Lob" -A3 src/main/java}.
     *
     * <p>Está fixado de propósito. Acrescentar um {@code @Lob} novo faz este teste falhar, e é
     * assim que se pretende: {@code @Lob} sobre {@code String} em PostgreSQL é uma decisão de
     * persistência com consequências operacionais (armazenamento em {@code pg_largeobject},
     * impossibilidade de semear por SQL, objetos órfãos que o {@code DROP SCHEMA CASCADE} não
     * apaga), e deve passar por uma revisão explícita em vez de entrar em silêncio.
     */
    private static final Map<String, Set<String>> EXPECTED_LOB_COLUMNS = Map.ofEntries(
            Map.entry("t_activity_approval_history", Set.of("comment")),
            Map.entry("t_change_requests",
                    Set.of("current_value", "field_name", "justification", "proposed_value", "reviewer_comment")),
            Map.entry("t_institutional_identity", Set.of("mission", "values_json", "vision")),
            Map.entry("t_key_result_checkins", Set.of("comment")),
            Map.entry("t_siadap_evaluations", Set.of("last_negotiation_comment")),
            Map.entry("t_sigof_sync_log", Set.of("error_message")),
            Map.entry("t_simulation_results", Set.of("details")),
            Map.entry("t_simulation_scenarios", Set.of("parameters")),
            Map.entry("t_strategic_goals", Set.of("description")),
            Map.entry("t_strategic_indicators", Set.of("evaluation_criteria")),
            Map.entry("t_tactical_activities",
                    Set.of("description_what", "justification_why", "methodology_how")));

    @Test
    @DisplayName("O inventário de colunas @Lob é lido do código e cobre as 11 entidades conhecidas")
    void inventoryIsDerivedFromTheCodeAndCoversTheKnownEntities() {
        Map<String, Set<String>> actual = LobColumnSqlGuard.scanLobColumns(JAVA_SOURCE_ROOT);

        System.out.println("[guarda @Lob] colunas encontradas em " + JAVA_SOURCE_ROOT + ":");
        LobColumnSqlGuard.scanLobColumnDetails(JAVA_SOURCE_ROOT)
                .forEach(lob -> System.out.println("  " + lob.table() + "." + lob.column()
                        + "  (" + lob.declaredIn() + ")"));

        assertEquals(KNOWN_TABLE_COUNT, actual.size(),
                "Mudou o conjunto de tabelas com colunas @Lob. Tabelas encontradas: " + actual.keySet());
        assertEquals(EXPECTED_LOB_COLUMNS, actual,
                """
                O inventário de colunas @Lob divergiu do esperado.
                Se acrescentaste um @Lob: confirma que é mesmo necessário -- em PostgreSQL implica \
                armazenamento em objeto grande, e a coluna deixa de poder ser semeada por SQL -- e \
                atualiza EXPECTED_LOB_COLUMNS.
                Se removeste um @Lob: atualiza EXPECTED_LOB_COLUMNS; a coluna passa a poder ser \
                semeada normalmente.""");
    }

    @Test
    @DisplayName("A guarda reprova o INSERT de 4884af69 nas três colunas @Lob da identidade")
    void guardRejectsTheInsertFromTheOriginalIncident() {
        assertTrue(Files.isRegularFile(REGRESSION_FIXTURE),
                "Falta a fixture de regressão " + REGRESSION_FIXTURE + " -- sem ela, a guarda só está "
                        + "provada no caso verde, que é como um detetor passa a verde sobre o ficheiro "
                        + "que tem o defeito.");

        List<LobColumnSqlGuard.Violation> violations = LobColumnSqlGuard.findViolations(
                LobColumnSqlGuard.scanLobColumns(JAVA_SOURCE_ROOT), List.of(REGRESSION_FIXTURE));

        System.out.println("[guarda @Lob] caso vermelho, " + REGRESSION_FIXTURE + ":");
        violations.forEach(violation -> System.out.println("  " + violation));

        assertEquals(Set.of("mission", "vision", "values_json"),
                violations.stream().map(LobColumnSqlGuard.Violation::column).collect(Collectors.toSet()),
                "A guarda deixou de detetar o INSERT que originou o incidente.");
        assertTrue(violations.stream().allMatch(v -> "t_institutional_identity".equals(v.table())),
                "Violações em tabela inesperada: " + violations);
    }

    @Test
    @DisplayName("Nenhum ficheiro .sql do repositório escreve numa coluna @Lob")
    void noSqlFileInTheRepositoryWritesToALobColumn() {
        List<Path> sqlFiles = LobColumnSqlGuard.listSqlFiles(REPOSITORY_ROOT);

        System.out.println("[guarda @Lob] caso verde, " + sqlFiles.size() + " ficheiros .sql varridos:");
        sqlFiles.forEach(file -> System.out.println("  " + file.toString().replace('\\', '/')));

        assertFalse(sqlFiles.isEmpty(), "Nenhum ficheiro .sql varrido -- a guarda estaria verde por vazio.");
        assertTrue(containsFileNamed(sqlFiles, "seed_identidade.sql"),
                "O seed da identidade não foi varrido: " + sqlFiles);
        assertTrue(containsFileNamed(sqlFiles, "V26__bsc_perspective_config.sql"),
                "As migrations Flyway não foram varridas: " + sqlFiles);
        assertFalse(containsFileNamed(sqlFiles, REGRESSION_FIXTURE.getFileName().toString()),
                "A fixture de regressão tem de ficar fora do varrimento do repositório.");

        List<LobColumnSqlGuard.Violation> violations =
                LobColumnSqlGuard.findViolations(LobColumnSqlGuard.scanLobColumns(JAVA_SOURCE_ROOT), sqlFiles);

        assertEquals(List.of(), violations,
                """
                SQL cru a escrever numa coluna @Lob. No PostgreSQL o Hibernate guarda nessa coluna o \
                OID de um objeto grande e lê-a com getLong(); com texto lá dentro, TODAS as leituras \
                da tabela passam a rebentar com SQLState 22003 -> HTTP 400, mesmo as que não tocam no \
                campo. Foi o incidente 4884af69, que bloqueou o percurso BSC inteiro.
                Cria estas linhas pela API (é o único caminho que escreve a representação certa) ou \
                deixa de anotar a coluna com @Lob.
                Violações:""");
    }

    private static boolean containsFileNamed(List<Path> files, String fileName) {
        return files.stream().anyMatch(file -> file.getFileName().toString().equals(fileName));
    }
}
