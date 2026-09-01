package cv.igrp.RH_Service.shared.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Varre o código Java à procura de colunas anotadas com {@code @Lob} e varre os ficheiros SQL do
 * repositório à procura de DML que escreva nessas colunas.
 *
 * <p><strong>Porquê.</strong> No PostgreSQL, o Hibernate materializa um {@code @Lob} sobre
 * {@code String} como <em>objeto grande</em>: cria a entrada em {@code pg_largeobject} e grava na
 * coluna {@code text} apenas o OID. Na leitura chama {@code getLong()} sobre essa coluna. Um
 * {@code INSERT} em SQL cru põe lá o texto e o {@code getLong()} rebenta com SQLState 22003, que
 * sobe como {@code DataIntegrityViolationException} e sai como HTTP 400. O defeito não se
 * manifesta na escrita — só na leitura, e para <em>todas</em> as leituras da tabela. Foi o que
 * aconteceu com {@code t_institutional_identity} (commit 4884af69, revertido por 5e819bfb):
 * bloqueou o {@code GetCurrentStrategyMapQueryHandler} e o
 * {@code CreateStrategicGoalCommandHandler}, ou seja, o percurso BSC inteiro.
 *
 * <p><strong>Âmbito.</strong> O inventário é derivado do código, não escrito à mão: qualquer
 * {@code @Lob} novo entra na guarda sem que ninguém se lembre de a atualizar. O varrimento cobre
 * <em>todos</em> os ficheiros {@code .sql} do repositório — migrations Flyway incluídas, porque
 * uma migration que semeie uma coluna {@code @Lob} corrompe exatamente da mesma maneira.
 *
 * <p><strong>Limites conhecidos.</strong> É análise de texto, não um parser de SQL. Não segue
 * {@code INSERT ... SELECT} com lista de colunas implícita na origem, não resolve tabelas criadas
 * dinamicamente nem SQL montado em Java, e trata as tabelas de auditoria do Envers
 * ({@code *_aud}) como a tabela base. Deteta a forma pela qual o defeito real entrou; não prova a
 * ausência de todas as formas possíveis.
 */
final class LobColumnSqlGuard {

    /** Pastas que nunca são varridas: artefactos de build, metadados e as fixtures da própria guarda. */
    private static final Set<String> EXCLUDED_DIRECTORIES =
            Set.of("target", ".git", ".idea", "node_modules", "lobguard");

    private static final Pattern TABLE_ANNOTATION =
            Pattern.compile("@Table\\s*\\([^)]*name\\s*=\\s*\"([^\"]+)\"");

    private static final Pattern COLUMN_ANNOTATION_NAME =
            Pattern.compile("@Column\\s*\\([^)]*name\\s*=\\s*\"([^\"]+)\"");

    private static final Pattern FIELD_DECLARATION =
            Pattern.compile("^\\s*(?:private|protected|public)\\s+[\\w<>,.\\[\\]\\s]+?(\\w+)\\s*[;=]");

    private static final Pattern INSERT_STATEMENT =
            Pattern.compile("insert\\s+into\\s+([\\w.\"]+)\\s*(?:\\(([^)]*)\\))?",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern UPDATE_STATEMENT =
            Pattern.compile("update\\s+(?:only\\s+)?([\\w.\"]+)\\s+set\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern COPY_STATEMENT =
            Pattern.compile("copy\\s+([\\w.\"]+)\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);

    private LobColumnSqlGuard() {
    }

    /** Uma coluna {@code @Lob}: tabela, coluna e o ficheiro Java que a declara. */
    record LobColumn(String table, String column, String declaredIn) {
    }

    /** Uma escrita SQL sobre uma coluna {@code @Lob}. */
    record Violation(Path file, int line, String table, String column, String statement) {

        @Override
        public String toString() {
            return "%s:%d  %s  ->  %s.%s".formatted(
                    file.toString().replace('\\', '/'), line, statement, table, column);
        }
    }

    /**
     * Inventário {@code tabela -> colunas @Lob}, lido das entidades JPA em {@code src/main/java}.
     */
    static Map<String, Set<String>> scanLobColumns(Path javaSourceRoot) {
        Map<String, Set<String>> inventory = new TreeMap<>();
        for (LobColumn lobColumn : scanLobColumnDetails(javaSourceRoot)) {
            inventory.computeIfAbsent(lobColumn.table(), key -> new TreeSet<>()).add(lobColumn.column());
        }
        return inventory;
    }

    static List<LobColumn> scanLobColumnDetails(Path javaSourceRoot) {
        List<LobColumn> found = new ArrayList<>();
        for (Path javaFile : listFiles(javaSourceRoot, ".java")) {
            List<String> lines = readLines(javaFile);
            String table = findTableName(lines);
            if (table == null) {
                continue;
            }
            for (int i = 0; i < lines.size(); i++) {
                if (!lines.get(i).trim().startsWith("@Lob")) {
                    continue;
                }
                String column = resolveColumnName(lines, i);
                if (column != null) {
                    found.add(new LobColumn(table, column, javaFile.getFileName().toString()));
                }
            }
        }
        found.sort((left, right) -> {
            int byTable = left.table().compareTo(right.table());
            return byTable != 0 ? byTable : left.column().compareTo(right.column());
        });
        return found;
    }

    /** Todos os ficheiros {@code .sql} sob {@code root}, excluindo as pastas de {@link #EXCLUDED_DIRECTORIES}. */
    static List<Path> listSqlFiles(Path root) {
        return listFiles(root, ".sql");
    }

    /** Escritas SQL sobre colunas {@code @Lob}, para os ficheiros indicados. */
    static List<Violation> findViolations(Map<String, Set<String>> lobColumns, List<Path> sqlFiles) {
        List<Violation> violations = new ArrayList<>();
        for (Path sqlFile : sqlFiles) {
            violations.addAll(findViolations(lobColumns, sqlFile, blankOutNoise(readContent(sqlFile))));
        }
        return violations;
    }

    private static List<Violation> findViolations(Map<String, Set<String>> lobColumns, Path file, String sql) {
        List<Violation> violations = new ArrayList<>();

        Matcher insert = INSERT_STATEMENT.matcher(sql);
        while (insert.find()) {
            String table = normalizeTable(insert.group(1));
            Set<String> lobs = lobColumns.get(table);
            if (lobs == null) {
                continue;
            }
            int line = lineOf(sql, insert.start());
            if (insert.group(2) == null) {
                // Sem lista de colunas, o INSERT escreve a linha toda -- logo, escreve os @Lob todos.
                for (String lob : lobs) {
                    violations.add(new Violation(file, line, table, lob, "INSERT INTO (sem lista de colunas)"));
                }
                continue;
            }
            for (String column : splitColumns(insert.group(2))) {
                if (lobs.contains(column)) {
                    violations.add(new Violation(file, line, table, column, "INSERT INTO"));
                }
            }
        }

        Matcher update = UPDATE_STATEMENT.matcher(sql);
        while (update.find()) {
            String table = normalizeTable(update.group(1));
            Set<String> lobs = lobColumns.get(table);
            if (lobs == null) {
                continue;
            }
            String assignments = sql.substring(update.end(), endOfStatement(sql, update.end()));
            for (String lob : lobs) {
                if (Pattern.compile("\\b" + Pattern.quote(lob) + "\\s*=", Pattern.CASE_INSENSITIVE)
                        .matcher(assignments).find()) {
                    violations.add(new Violation(file, lineOf(sql, update.start()), table, lob, "UPDATE ... SET"));
                }
            }
        }

        Matcher copy = COPY_STATEMENT.matcher(sql);
        while (copy.find()) {
            String table = normalizeTable(copy.group(1));
            Set<String> lobs = lobColumns.get(table);
            if (lobs == null) {
                continue;
            }
            for (String column : splitColumns(copy.group(2))) {
                if (lobs.contains(column)) {
                    violations.add(new Violation(file, lineOf(sql, copy.start()), table, column, "COPY"));
                }
            }
        }

        return violations;
    }

    private static String findTableName(List<String> lines) {
        for (String line : lines) {
            Matcher matcher = TABLE_ANNOTATION.matcher(line);
            if (matcher.find()) {
                return normalizeTable(matcher.group(1));
            }
        }
        return null;
    }

    /**
     * Nome da coluna que se segue a um {@code @Lob}: o {@code name} do {@code @Column} se existir,
     * senão o nome do campo convertido de camelCase para snake_case (a estratégia de nomes por
     * omissão do Hibernate no Spring Boot).
     */
    private static String resolveColumnName(List<String> lines, int lobLineIndex) {
        for (int i = lobLineIndex + 1; i < Math.min(lines.size(), lobLineIndex + 8); i++) {
            String line = lines.get(i);
            Matcher column = COLUMN_ANNOTATION_NAME.matcher(line);
            if (column.find()) {
                return column.group(1).toLowerCase(Locale.ROOT);
            }
            Matcher field = FIELD_DECLARATION.matcher(line);
            if (field.find()) {
                return camelToSnake(field.group(1));
            }
        }
        return null;
    }

    private static String camelToSnake(String name) {
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }

    /**
     * Normaliza o nome de tabela: sem aspas, sem prefixo de esquema, em minúsculas e sem o sufixo
     * {@code _aud} das tabelas de auditoria do Envers, que espelham as colunas {@code @Lob} da
     * tabela base e sofrem da mesma corrupção.
     */
    private static String normalizeTable(String raw) {
        String table = raw.replace("\"", "").toLowerCase(Locale.ROOT);
        int lastDot = table.lastIndexOf('.');
        if (lastDot >= 0) {
            table = table.substring(lastDot + 1);
        }
        return table.endsWith("_aud") ? table.substring(0, table.length() - "_aud".length()) : table;
    }

    private static Set<String> splitColumns(String columnList) {
        Set<String> columns = new LinkedHashSet<>();
        for (String column : columnList.split(",")) {
            String cleaned = column.trim().replace("\"", "").toLowerCase(Locale.ROOT);
            if (!cleaned.isEmpty()) {
                columns.add(cleaned);
            }
        }
        return columns;
    }

    /**
     * Substitui por espaços o conteúdo de comentários ({@code --} e {@code /* *}{@code /}) e de
     * literais de texto, preservando os fins de linha. Sem isto, um comentário que <em>descreva</em>
     * o INSERT proibido — como o que hoje explica a armadilha no {@code seed_identidade.sql} —
     * seria contado como violação, e a guarda que grita sobre a sua própria documentação acaba
     * desligada.
     */
    private static String blankOutNoise(String sql) {
        char[] out = sql.toCharArray();
        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        for (int i = 0; i < out.length; i++) {
            char current = out[i];
            char next = i + 1 < out.length ? out[i + 1] : '\0';
            if (inLineComment) {
                if (current == '\n') {
                    inLineComment = false;
                } else {
                    out[i] = ' ';
                }
            } else if (inBlockComment) {
                if (current == '*' && next == '/') {
                    out[i] = ' ';
                    out[++i] = ' ';
                    inBlockComment = false;
                } else if (current != '\n') {
                    out[i] = ' ';
                }
            } else if (inString) {
                if (current == '\'') {
                    inString = false;
                } else if (current != '\n') {
                    out[i] = ' ';
                }
            } else if (current == '-' && next == '-') {
                out[i] = ' ';
                out[++i] = ' ';
                inLineComment = true;
            } else if (current == '/' && next == '*') {
                out[i] = ' ';
                out[++i] = ' ';
                inBlockComment = true;
            } else if (current == '\'') {
                inString = true;
            }
        }
        return new String(out);
    }

    private static int endOfStatement(String sql, int from) {
        int semicolon = sql.indexOf(';', from);
        return semicolon < 0 ? sql.length() : semicolon;
    }

    private static int lineOf(String text, int offset) {
        int line = 1;
        for (int i = 0; i < offset; i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private static List<Path> listFiles(Path root, String extension) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(extension))
                    .filter(LobColumnSqlGuard::isNotExcluded)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isNotExcluded(Path path) {
        for (Path part : path) {
            if (EXCLUDED_DIRECTORIES.contains(part.toString())) {
                return false;
            }
        }
        return true;
    }

    private static List<String> readLines(Path path) {
        return List.of(readContent(path).split("\n", -1));
    }

    private static String readContent(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
