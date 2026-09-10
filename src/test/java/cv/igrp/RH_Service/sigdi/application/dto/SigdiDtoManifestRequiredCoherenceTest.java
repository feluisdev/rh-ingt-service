package cv.igrp.RH_Service.sigdi.application.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Portão manifesto {@literal ->} Java, generalizado a <strong>todos</strong> os manifestos DTO
 * do módulo {@code sigdi} ({@code 136-01-PLAN.md}, Task 2, {@code D-51}). Restrito a uma única
 * pergunta, deliberadamente estreita: um atributo com {@code "required": true} no manifesto tem,
 * no campo Java homónimo, {@code @NotNull} ou {@code @NotBlank}?
 *
 * <p>O nome do ficheiro de manifesto é a autoridade para localizar a classe Java
 * ({@code X.json -> cv.igrp.RH_Service.sigdi.application.dto.X}) -- a chave {@code "name"}
 * dentro do manifesto não coincide com o nome do ficheiro em vários casos (p.ex.
 * {@code CreateTacticalActivityDTO.json} declara {@code "name": "CreateTacticalActivity"}).
 *
 * <p><strong>O que este teste NÃO promete, com a mesma honestidade do
 * {@code WorkflowInboxItemDtoManifestCoherenceTest} de que este é a generalização:</strong>
 * <ul>
 *   <li>não corre o gerador do IGRP Studio e não garante que qualquer {@code .java} deste
 *       módulo é o que o gerador emitiria hoje;</li>
 *   <li><strong>deteta</strong> o caso estreito em que um manifesto exige um campo
 *       ({@code "required": true}) e o {@code .java} correspondente já não valida a
 *       obrigatoriedade -- o padrão exato do {@code A-135-2AA};</li>
 *   <li><strong>não deteta</strong> uma regeneração que reescreva manifesto e {@code .java}
 *       juntos a partir de uma definição desatualizada -- nesse caso os dois ficheiros
 *       continuam coerentes um com o outro e este teste fica verde na mesma;</li>
 *   <li>não é prevenção nenhuma: nada neste repositório impede uma regeneração de sobrescrever
 *       manifesto e {@code .java}. O {@code "readOnly": true} nos manifestos é declaração de
 *       intenção -- o gerador de DTOs ({@code references/addDTO.md}) não tem regra de
 *       enforcement, ao contrário dos enums ({@code references/addEnum.md:44,57});</li>
 *   <li>só verifica UMA direção (manifesto exige {@literal ->} Java valida). Não verifica se o
 *       conjunto de campos é o mesmo dos dois lados (isso é o
 *       {@code WorkflowInboxItemDtoManifestCoherenceTest}, por manifesto, não generalizado), nem
 *       se um atributo não-obrigatório tem a MESMA riqueza de validação (mínimos, máximos,
 *       intervalos) -- ver {@code 136-MANIFESTOS.md}, secção "o manifesto não sabe exprimir a
 *       regra", para o caso nomeado do {@code weight} de {@code CreateStategicGoalDTO}/
 *       {@code UpdateStategicGoalDTO}, que este teste deliberadamente não cobre.</li>
 * </ul>
 *
 * <p><strong>Lista de exceções:</strong> vazia. A varredura de {@code 136-MANIFESTOS.md}
 * (25 manifestos com pelo menos um {@code "required": true}, 66 atributos obrigatórios no
 * total) encontrou cinco divergências, e as cinco eram "manifesto exige a mais" -- corrigidas
 * nos próprios manifestos por este plano ({@code AssignBudgetDTO}, {@code CloseEvaluationsRequestDTO},
 * {@code CreateTacticalActivityDTO} x2, {@code KeyResultCheckinRequestDTO}). Nenhuma era "Java
 * valida a menos", pelo que não há, hoje, nenhuma entrada destinada ao {@code 136-09} por esta
 * via. Se o {@code 136-09} vier a precisar de uma exceção aqui (um caso em que a correção do lado
 * Java fica pendente e o portão teria de ficar vermelho até lá), acrescenta-a a
 * {@link #EXCECOES} com o identificador do achado e a razão -- nunca apagues o atributo do
 * manifesto para "resolver" a divergência sem essa entrada.
 */
class SigdiDtoManifestRequiredCoherenceTest {

    private static final String MANIFEST_DIR = ".igrpstudio/sigdi/dto";
    private static final String DTO_PACKAGE = "cv.igrp.RH_Service.sigdi.application.dto.";

    /**
     * Exceções ao portão: {@code "Manifesto.json#atributo"} -> razão + identificador do achado.
     * Uma entrada aqui faz o teste tolerar a divergência em vez de falhar -- usa-se só quando a
     * correção do lado Java está deliberadamente fora deste plano (destino {@code 136-09}).
     * Vazia hoje: ver Javadoc da classe.
     */
    private static final Map<String, String> EXCECOES = Map.of();

    @Test
    void requiredManifestAttributesHaveMatchingJavaValidation() throws IOException {
        File dir = new File(MANIFEST_DIR);
        File[] manifestFiles = dir.listFiles((d, name) -> name.endsWith(".json"));
        assertTrue(manifestFiles != null && manifestFiles.length > 0,
                "Nenhum manifesto encontrado em " + MANIFEST_DIR);

        ObjectMapper mapper = new ObjectMapper();
        List<String> divergences = new ArrayList<>();
        int manifestsScanned = 0;
        int requiredAttributesScanned = 0;

        for (File manifestFile : manifestFiles) {
            manifestsScanned++;
            String manifestFileName = manifestFile.getName();
            String className = manifestFileName.substring(0, manifestFileName.length() - ".json".length());

            JsonNode root = mapper.readTree(manifestFile);
            JsonNode attributes = root.get("attributes");
            if (attributes == null || !attributes.isArray()) {
                continue; // manifestos de outro tipo/forma -- fora do âmbito desta pergunta
            }

            Class<?> dtoClass;
            try {
                dtoClass = Class.forName(DTO_PACKAGE + className);
            } catch (ClassNotFoundException e) {
                divergences.add(manifestFileName + ": manifesto órfão -- não existe "
                        + DTO_PACKAGE + className + " no classpath");
                continue;
            }

            for (JsonNode attribute : attributes) {
                JsonNode requiredNode = attribute.get("required");
                if (requiredNode == null || !requiredNode.asBoolean(false)) {
                    continue;
                }
                requiredAttributesScanned++;
                String attributeName = attribute.get("name").asText();
                String exceptionKey = manifestFileName + "#" + attributeName;
                if (EXCECOES.containsKey(exceptionKey)) {
                    continue;
                }

                Field field;
                try {
                    field = dtoClass.getDeclaredField(attributeName);
                } catch (NoSuchFieldException e) {
                    divergences.add(manifestFileName + "#" + attributeName + ": \"required\": true no"
                            + " manifesto, mas " + className + ".java não tem campo com este nome --"
                            + " manifesto exige um campo que o Java não declara");
                    continue;
                }

                boolean hasNotNull = field.isAnnotationPresent(jakarta.validation.constraints.NotNull.class);
                boolean hasNotBlank = field.isAnnotationPresent(jakarta.validation.constraints.NotBlank.class);
                if (!hasNotNull && !hasNotBlank) {
                    divergences.add(manifestFileName + "#" + attributeName + ": \"required\": true no"
                            + " manifesto, mas " + className + "." + attributeName + " não tem"
                            + " @NotNull nem @NotBlank -- manifesto exige a mais");
                }
            }
        }

        assertTrue(manifestsScanned >= 25,
                "Esperava pelo menos 25 manifestos em " + MANIFEST_DIR + ", encontrou " + manifestsScanned);

        if (!divergences.isEmpty()) {
            fail("Divergência(s) manifesto -> Java na chave \"required\" (" + divergences.size() + "):\n  - "
                    + String.join("\n  - ", divergences));
        }
    }
}
