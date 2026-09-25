package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * O portão do {@code A-135-03} (Fase 136, plano 07): {@code SpringCommandBus.send()} não corre
 * <i>Bean Validation</i> sobre objetos {@code Command} -- só faz <i>dispatch</i> por classe. Uma
 * anotação {@code jakarta.validation.constraints} num campo de uma classe {@code Command} deste
 * pacote é, portanto, uma garantia que o código promete e nunca cumpre -- exatamente o defeito
 * que este plano corrigiu em quinze classes (as oito fichadas em {@code A-135-03} mais sete da
 * mesma família, achadas ao construir este próprio teste). Este teste é o que impede a
 * dezasseis: varre <b>todas</b> as classes que implementam {@code Command} no pacote, derivadas
 * da fonte, e falha se alguma declarar um campo anotado com uma restrição de
 * {@code jakarta.validation.constraints}.
 *
 * <p><b>A única forma legítima de uma anotação sobreviver aqui</b> é a classe ser, ela própria,
 * o parâmetro {@code @Valid @RequestBody} de algum método de controlador -- nesse caso, e só
 * nesse, o Spring corre a validação de bean sobre o objeto antes de ele ser despachado (ver
 * {@code UpdateTacticalActivityCommandValidationTest}, o precedente {@code 613207ed} da Fase
 * 135). {@link #LEGITIMATE_EXCEPTIONS} está vazio hoje: {@code UpdateTacticalActivityCommand} é
 * a única classe do módulo nessa posição, confirmado por leitura de
 * {@code TaticalController.updateTacticalActivity}, e não carrega nenhuma anotação de validação
 * -- o precedente já as removeu. Uma entrada só pode ser acrescentada com a razão escrita ao
 * lado, confirmada por leitura direta do controlador citado -- nunca por conveniência.
 */
class SigdiCommandNoBeanValidationTest {

  private static final Path COMMANDS_DIR = Path.of(
      "src", "main", "java", "cv", "igrp", "RH_Service", "sigdi", "application", "commands");

  private static final Pattern COMMAND_CLASS_PATTERN =
      Pattern.compile("class\\s+(\\w+)\\s+implements\\s+Command\\b");

  /**
   * Captura o bloco de anotações imediatamente antes de uma declaração de campo {@code private}
   * e o nome do campo. O tipo entre {@code private} e o nome do campo é lido de forma
   * não-gananciosa até ao {@code ;} -- não precisa de reconhecer genéricos, arrays nem
   * comentários, porque nenhum campo de nenhuma classe Command deste pacote os usa.
   */
  private static final Pattern ANNOTATED_FIELD_PATTERN = Pattern.compile(
      "((?:@[A-Za-z]+(?:\\([^)]*\\))?\\s*)+)private\\b[^;]*?\\b(\\w+)\\s*;");

  private static final Pattern SINGLE_ANNOTATION_PATTERN = Pattern.compile("@(\\w+)");

  /**
   * Os nomes simples das restrições de {@code jakarta.validation.constraints} -- não inclui
   * {@code @Valid}, que não é uma restrição em si, apenas um marcador de cascata (sem efeito
   * nenhum se o objeto que o carrega nunca for validado).
   */
  private static final Set<String> BEAN_VALIDATION_CONSTRAINT_ANNOTATIONS = Set.of(
      "NotNull", "NotBlank", "NotEmpty", "Size", "Min", "Max", "Pattern", "Email",
      "Positive", "PositiveOrZero", "Negative", "NegativeOrZero", "Past", "PastOrPresent",
      "Future", "FutureOrPresent", "Digits", "DecimalMin", "DecimalMax",
      "AssertTrue", "AssertFalse");

  /**
   * Ver o javadoc de classe. Vazio de propósito -- nenhuma classe Command deste pacote precisa
   * hoje de uma exceção.
   */
  private static final Map<String, String> LEGITIMATE_EXCEPTIONS = Map.of();

  private static final Pattern LINE_COMMENT_PATTERN = Pattern.compile("//[^\\n]*");

  private static String readFile(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read " + path.toAbsolutePath(), e);
    }
  }

  /**
   * Lê o ficheiro com todos os comentários {@code //} apagados até ao fim da linha -- incluindo
   * os que partilham linha com uma anotação (ex.: {@code @NotBlank // nota}). Sem isto,
   * {@link #ANNOTATED_FIELD_PATTERN} exigiria apenas espaço em branco entre a anotação e
   * {@code private}, e um comentário na mesma linha esconderia a anotação do varrimento -- o
   * inverso exato do que este portão existe para impedir.
   */
  private static String readFileWithoutLineComments(Path path) {
    return LINE_COMMENT_PATTERN.matcher(readFile(path)).replaceAll("");
  }

  /**
   * Todas as classes que implementam {@code Command} no pacote, derivadas da fonte -- não de uma
   * lista escrita à mão que ficaria desatualizada na próxima vez que alguém acrescentar um
   * comando. Devolve um mapa ordenado por ficheiro para que as falhas apareçam sempre pela mesma
   * ordem.
   */
  private static Map<Path, String> deriveCommandClasses() {
    if (!Files.isDirectory(COMMANDS_DIR)) {
      fail("Diretório de comandos não encontrado: " + COMMANDS_DIR.toAbsolutePath());
    }
    Map<Path, String> classes = new TreeMap<>(Comparator.comparing(Path::toString));
    try (Stream<Path> files = Files.list(COMMANDS_DIR)) {
      for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
        Matcher matcher = COMMAND_CLASS_PATTERN.matcher(readFile(file));
        if (matcher.find()) {
          classes.put(file, matcher.group(1));
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to list " + COMMANDS_DIR.toAbsolutePath(), e);
    }
    return classes;
  }

  /**
   * Guarda contra um regex que parasse de corresponder e encolhesse o varrimento em silêncio --
   * o pacote tinha 56 classes {@code Command} quando este teste foi escrito (plano 07, Fase
   * 136); o limiar fica abaixo desse número para não quebrar com adições futuras legítimas.
   */
  @Test
  void theDerivedCommandSetDoesNotSilentlyShrink() {
    Map<Path, String> commands = deriveCommandClasses();
    assertTrue(commands.size() >= 50,
        "Esperava pelo menos 50 classes Command no pacote sigdi.application.commands -- um "
            + "regex que parasse de corresponder encolheria isto em silêncio. Encontradas: "
            + commands.size() + " -- " + commands.values());
  }

  @Test
  void noCommandClassDeclaresAnUnreachableBeanValidationAnnotation() {
    List<String> failures = new ArrayList<>();
    for (Map.Entry<Path, String> entry : deriveCommandClasses().entrySet()) {
      Path file = entry.getKey();
      String className = entry.getValue();
      String legitimateReason = LEGITIMATE_EXCEPTIONS.get(className);

      Matcher fieldMatcher = ANNOTATED_FIELD_PATTERN.matcher(readFileWithoutLineComments(file));
      while (fieldMatcher.find()) {
        String annotationsBlock = fieldMatcher.group(1);
        String fieldName = fieldMatcher.group(2);

        Matcher annotationMatcher = SINGLE_ANNOTATION_PATTERN.matcher(annotationsBlock);
        while (annotationMatcher.find()) {
          String annotationName = annotationMatcher.group(1);
          if (!BEAN_VALIDATION_CONSTRAINT_ANNOTATIONS.contains(annotationName)) {
            continue;
          }
          if (legitimateReason != null) {
            // Exceção documentada: esta classe é o próprio parâmetro @Valid @RequestBody de
            // um controlador, logo a anotação é avaliada de facto.
            continue;
          }
          failures.add(className + "." + fieldName + " declara @" + annotationName
              + " (" + file + ")");
        }
      }
    }

    assertTrue(failures.isEmpty(),
        "As seguintes classes Command do módulo sigdi declaram um campo com anotação de "
            + "jakarta.validation.constraints que o SpringCommandBus nunca avalia -- "
            + "SpringCommandBus.send() não corre Bean Validation sobre objetos Command, só faz "
            + "dispatch por classe. A anotação pertence ao DTO que o comando embrulha (esse sim "
            + "chega, separadamente, a um parâmetro @Valid @RequestBody do controlador), não ao "
            + "wrapper Command. Remove a anotação daqui, seguindo o precedente 613207ed (Fase "
            + "135, UpdateTacticalActivityCommand) e SIA-06 (Fase 113) -- ou, se esta classe for "
            + "mesmo o próprio parâmetro @Valid @RequestBody de um controlador, acrescenta-a a "
            + "LEGITIMATE_EXCEPTIONS nesta classe de teste, com a razão escrita ao lado, "
            + "confirmada por leitura do controlador. Falhas: " + failures);
  }
}
