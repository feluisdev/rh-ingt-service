package cv.igrp.RH_Service.shared.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
 * <p>Os seis testes desta classe têm papéis distintos e nenhum deles é dispensável:
 * <ol>
 *   <li>o inventário prova que o detetor <em>vê</em> as colunas — sem isto, um detetor com o
 *       inventário vazio passa a verde sobre qualquer ficheiro;</li>
 *   <li>o caso vermelho prova que o detetor <em>reprova</em> o defeito real, corrido contra a cópia
 *       literal do {@code INSERT} de 4884af69;</li>
 *   <li>o caso verde prova que o estado atual do repositório está limpo;</li>
 *   <li>a prova do {@code blankOutNoise} mede, nos dois sentidos, o caso em que o filtro de
 *       comentários é a diferença entre apanhar e não apanhar;</li>
 *   <li>a tabela de formas de DML fixa <em>o que</em> a guarda apanha e <em>o que não</em> apanha —
 *       cada limite conhecido é medido aqui, não presumido num comentário;</li>
 *   <li>o limite «só vê ficheiros {@code .sql}» é medido pela ausência, que é a única forma de o
 *       medir.</li>
 * </ol>
 */
class LobColumnSqlGuardTest {

    private static final int KNOWN_TABLE_COUNT = 11;

    private static final Path REPOSITORY_ROOT = Path.of(".");
    private static final Path JAVA_SOURCE_ROOT = Path.of("src", "main", "java");
    private static final Path LOBGUARD_FIXTURES = Path.of("src", "test", "resources", "lobguard");
    private static final Path REGRESSION_FIXTURE =
            LOBGUARD_FIXTURES.resolve("regression-4884af69-institutional-identity.sql");
    private static final Path DOCUMENTING_COMMENT_FIXTURE =
            LOBGUARD_FIXTURES.resolve("comment-documents-the-trap.sql");

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
        Map<String, Set<String>> lobColumns = LobColumnSqlGuard.scanLobColumns(JAVA_SOURCE_ROOT);
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
        assertFalse(containsFileNamed(sqlFiles, DOCUMENTING_COMMENT_FIXTURE.getFileName().toString()),
                "A fixture do comentário tem de ficar fora do varrimento do repositório.");

        List<LobColumnSqlGuard.Violation> violations =
                LobColumnSqlGuard.findViolations(lobColumns, sqlFiles);

        // Medição, não presunção: quanto é que o filtro de comentários e literais está a poupar
        // HOJE, sobre este repositório. Impressa em cada build para que a afirmação da entrega
        // ("hoje o filtro não muda nada aqui") volte a ser medida sempre que alguém lê o log.
        List<LobColumnSqlGuard.Violation> withoutFilter = new ArrayList<>();
        for (Path sqlFile : sqlFiles) {
            withoutFilter.addAll(LobColumnSqlGuard.findViolations(
                    lobColumns, sqlFile, LobColumnSqlGuard.readContent(sqlFile)));
        }
        System.out.println("[guarda @Lob] blankOutNoise sobre o repositório: "
                + violations.size() + " violações com o filtro, " + withoutFilter.size()
                + " sem o filtro (diferença = " + (withoutFilter.size() - violations.size()) + ")");

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

    /**
     * O filtro de comentários e literais, medido nos dois sentidos sobre um ficheiro que existe para
     * isso.
     *
     * <p>Isto substitui uma afirmação que estava errada e que a auditoria de 2026-09-01 apanhou: a
     * entrega e o commit 2c86678f diziam que, sem o filtro, «o comentário que documenta a armadilha
     * no {@code seed_identidade.sql} seria ele próprio uma violação». Não seria — esse comentário
     * está escrito em prosa e nunca escreve a instrução. O caso verde imprime, em cada build, a
     * diferença real sobre o repositório, que hoje é zero.
     *
     * <p>O que é verdade, e é isto que este teste mede: o filtro é necessário para o comentário que
     * <em>cola</em> a instrução em vez de a parafrasear — que é o que a fixture faz, e é o que o
     * próximo autor fará quando quiser documentar a proibição no sítio onde ela morde.
     */
    @Test
    @DisplayName("Sem o blankOutNoise a guarda reprovaria a sua própria documentação — medido nos dois sentidos")
    void blankOutNoiseSparesCommentsThatQuoteTheForbiddenStatement() {
        assertTrue(Files.isRegularFile(DOCUMENTING_COMMENT_FIXTURE),
                "Falta a fixture " + DOCUMENTING_COMMENT_FIXTURE + " -- sem ela, a necessidade do "
                        + "blankOutNoise volta a ser uma presunção.");

        Map<String, Set<String>> lobColumns = LobColumnSqlGuard.scanLobColumns(JAVA_SOURCE_ROOT);
        String raw = LobColumnSqlGuard.readContent(DOCUMENTING_COMMENT_FIXTURE);

        List<LobColumnSqlGuard.Violation> withFilter = LobColumnSqlGuard.findViolations(
                lobColumns, DOCUMENTING_COMMENT_FIXTURE, LobColumnSqlGuard.blankOutNoise(raw));
        List<LobColumnSqlGuard.Violation> withoutFilter =
                LobColumnSqlGuard.findViolations(lobColumns, DOCUMENTING_COMMENT_FIXTURE, raw);

        System.out.println("[guarda @Lob] blankOutNoise sobre " + DOCUMENTING_COMMENT_FIXTURE + ": "
                + withFilter.size() + " com o filtro, " + withoutFilter.size() + " sem o filtro");
        withoutFilter.forEach(violation -> System.out.println("  [sem filtro] " + violation));

        assertEquals(List.of(), withFilter,
                "Com o filtro, um comentário que cita a instrução proibida não pode ser violação.");
        assertEquals(Set.of("mission", "vision", "values_json"),
                withoutFilter.stream().map(LobColumnSqlGuard.Violation::column).collect(Collectors.toSet()),
                "Sem o filtro, este comentário TEM de produzir violações -- é isso que justifica o "
                        + "blankOutNoise. Se deixou de produzir, o filtro perdeu a razão de existir e "
                        + "deve ser removido, não redocumentado.");
    }

    /**
     * A tabela de formas de DML: o que a guarda apanha e o que <em>não</em> apanha.
     *
     * <p>Existe pela razão pela qual a entrega anterior foi devolvida. A lista de «limites
     * conhecidos» era prosa: dizia que a guarda não seguia {@code INSERT ... SELECT} (segue), não
     * dizia que o {@code UPDATE} com alias escapava (escapava), e nunca tinha sido corrida contra
     * nenhuma destas formas. Aqui cada linha da lista é uma medição, e um limite que deixe de ser um
     * limite faz o teste falhar em vez de envelhecer em silêncio num comentário.
     */
    @Test
    @DisplayName("Cada forma de DML coberta, e cada limite conhecido, está medido e não presumido")
    void knownLimitsAreMeasuredNotPresumed() {
        Map<String, Set<String>> lobColumns = LobColumnSqlGuard.scanLobColumns(JAVA_SOURCE_ROOT);

        Map<String, String> apanha = new LinkedHashMap<>();
        apanha.put("INSERT com lista de colunas",
                "INSERT INTO t_strategic_goals (id, description) VALUES ('a', 'b');");
        apanha.put("INSERT sem lista de colunas",
                "INSERT INTO t_strategic_goals VALUES ('a', 'b');");
        apanha.put("INSERT sem espaco antes do parentesis",
                "INSERT INTO t_strategic_goals(id,description) VALUES ('a','b');");
        apanha.put("INSERT com alias AS",
                "INSERT INTO t_strategic_goals AS g (id, description) VALUES ('a','b');");
        apanha.put("INSERT ... SELECT com lista de colunas",
                "INSERT INTO t_strategic_goals (id, description) SELECT a, b FROM z;");
        apanha.put("INSERT ... SELECT sem lista de colunas",
                "INSERT INTO t_strategic_goals SELECT * FROM z;");
        apanha.put("INSERT ... ON CONFLICT DO UPDATE SET",
                "INSERT INTO t_strategic_goals (id) VALUES ('a') ON CONFLICT (id) DO UPDATE SET description = 'x';");
        apanha.put("esquema qualificado",
                "INSERT INTO public.t_strategic_goals (description) VALUES ('x');");
        apanha.put("tabela _aud do Envers",
                "INSERT INTO t_strategic_goals_aud (description) VALUES ('x');");
        apanha.put("identificadores entre aspas",
                "INSERT INTO \"t_strategic_goals\" (\"description\") VALUES ('x');");
        apanha.put("coluna em maiusculas",
                "INSERT INTO t_strategic_goals (ID, DESCRIPTION) VALUES ('a','b');");
        apanha.put("instrucao multilinha",
                "INSERT INTO t_strategic_goals\n  (id,\n   description)\nVALUES ('a','b');");
        apanha.put("corpo $$ ... $$ de funcao",
                "CREATE FUNCTION f() RETURNS void AS $$\nBEGIN\n  INSERT INTO t_strategic_goals (description)"
                        + " VALUES ('x');\nEND;\n$$ LANGUAGE plpgsql;");
        apanha.put("UPDATE simples",
                "UPDATE t_strategic_goals SET description = 'x';");
        apanha.put("UPDATE ONLY",
                "UPDATE ONLY t_strategic_goals SET description = 'x';");
        apanha.put("UPDATE com alias",
                "UPDATE t_strategic_goals g SET description = 'x';");
        apanha.put("UPDATE com alias AS",
                "UPDATE t_strategic_goals AS g SET description = 'x';");
        apanha.put("UPDATE ONLY com alias",
                "UPDATE ONLY t_strategic_goals g SET description = 'x';");
        apanha.put("UPDATE com alias e coluna qualificada",
                "UPDATE t_strategic_goals g SET g.description = 'x';");
        apanha.put("MERGE ... WHEN MATCHED THEN UPDATE SET",
                "MERGE INTO t_strategic_goals t USING s ON t.id = s.id"
                        + " WHEN MATCHED THEN UPDATE SET description = s.d;");
        apanha.put("MERGE ... WHEN NOT MATCHED THEN INSERT",
                "MERGE INTO t_strategic_goals t USING s ON t.id = s.id"
                        + " WHEN NOT MATCHED THEN INSERT (id, description) VALUES (s.id, s.d);");
        apanha.put("COPY com lista de colunas",
                "COPY t_strategic_goals (id, description) FROM stdin;");
        apanha.put("COPY sem lista de colunas",
                "COPY t_strategic_goals FROM stdin;");

        Map<String, String> naoApanha = new LinkedHashMap<>();
        // Limites conhecidos: cada um destes E um falso negativo, e esta aqui para o ser em voz alta.
        naoApanha.put("LIMITE: atribuicao multi-coluna no UPDATE",
                "UPDATE t_strategic_goals SET (description, name) = ('x', 'y');");
        naoApanha.put("LIMITE: tabela resolvida so em tempo de execucao",
                "EXECUTE format('INSERT INTO %I (description) VALUES ($1)', tabela);");
        // Formas que NAO sao escrita e que a guarda tem de ignorar -- o outro lado do mesmo teste.
        naoApanha.put("leitura: SELECT de uma coluna @Lob",
                "SELECT description FROM t_strategic_goals;");
        naoApanha.put("leitura: DELETE com coluna @Lob no WHERE",
                "DELETE FROM t_strategic_goals WHERE description = 'x';");
        naoApanha.put("leitura: COPY ... TO",
                "COPY t_strategic_goals (id, description) TO stdout;");
        naoApanha.put("DDL: ALTER TABLE sobre a coluna",
                "ALTER TABLE t_strategic_goals ALTER COLUMN description TYPE text;");
        naoApanha.put("tabela sem colunas @Lob",
                "INSERT INTO t_institutions (id, name) VALUES ('a', 'b');");
        naoApanha.put("coluna sem @Lob na tabela certa",
                "INSERT INTO t_strategic_goals (id, name) VALUES ('a', 'b');");

        List<String> falhas = new ArrayList<>();
        System.out.println("[guarda @Lob] tabela de formas de DML:");
        apanha.forEach((label, sql) -> {
            int n = LobColumnSqlGuard.findViolations(
                    lobColumns, Path.of("inline.sql"), LobColumnSqlGuard.blankOutNoise(sql)).size();
            System.out.printf("  apanha   n=%d  %s%n", n, label);
            if (n == 0) {
                falhas.add("DEIXOU DE APANHAR: " + label + "  ->  " + sql);
            }
        });
        naoApanha.forEach((label, sql) -> {
            int n = LobColumnSqlGuard.findViolations(
                    lobColumns, Path.of("inline.sql"), LobColumnSqlGuard.blankOutNoise(sql)).size();
            System.out.printf("  ignora   n=%d  %s%n", n, label);
            if (n != 0) {
                falhas.add("PASSOU A APANHAR: " + label + "  ->  " + sql);
            }
        });

        assertEquals(List.of(), falhas,
                """
                A tabela de formas de DML deixou de bater certo com o comportamento medido.
                "DEIXOU DE APANHAR" e uma regressao de cobertura: repara o padrao.
                "PASSOU A APANHAR" pode ser boa noticia -- um limite conhecido deixou de o ser, ou um \
                falso positivo novo. Decide qual, atualiza esta tabela E a lista de limites do \
                Javadoc de LobColumnSqlGuard, que tem de continuar a dizer a verdade.
                Divergencias:""");
    }

    /**
     * A guarda só vê ficheiros {@code .sql}. É o limite mais consequente da lista e o único que se
     * mede pela ausência: SQL montado em Java, {@code createNativeQuery}, {@code psql} à mão e
     * restauros de dump passam-lhe todos ao lado.
     */
    @Test
    @DisplayName("LIMITE medido: a guarda não vê SQL fora de ficheiros .sql")
    void guardDoesNotSeeSqlOutsideSqlFiles() {
        List<Path> scanned = LobColumnSqlGuard.listSqlFiles(REPOSITORY_ROOT);

        assertTrue(scanned.stream().allMatch(path -> path.toString().toLowerCase().endsWith(".sql")),
                "O varrimento passou a incluir ficheiros que não são .sql: " + scanned);
        assertFalse(scanned.stream().anyMatch(path -> path.toString().endsWith(".java")),
                "SQL em código Java continua fora do alcance da guarda -- se isto mudou, atualiza a "
                        + "lista de limites do Javadoc de LobColumnSqlGuard.");
    }

    private static boolean containsFileNamed(List<Path> files, String fileName) {
        return files.stream().anyMatch(file -> file.getFileName().toString().equals(fileName));
    }
}
