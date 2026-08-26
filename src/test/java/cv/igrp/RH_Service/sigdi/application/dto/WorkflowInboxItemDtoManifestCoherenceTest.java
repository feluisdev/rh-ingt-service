package cv.igrp.RH_Service.sigdi.application.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * Portão manifesto {@literal ->} DTO gerado, para {@code WorkflowInboxItemDTO} ({@code
 * 110-02-PLAN.md}, Task 1). O primeiro deste tipo no backend -- o frontend já tinha o
 * equivalente ({@code check-igrp-manifest-drift.mjs}).
 *
 * <p><strong>O que este teste NÃO promete, com a mesma honestidade do script do frontend:</strong>
 * <ul>
 *   <li>não corre o gerador do IGRP Studio e não garante que {@code WorkflowInboxItemDTO.java} é
 *       o que o gerador emitiria hoje;</li>
 *   <li><strong>deteta</strong> o caso estreito em que o {@code .java} diverge de um manifesto
 *       intocado -- alguém edita um dos dois ficheiros e esquece o outro;</li>
 *   <li><strong>não deteta</strong> o cenário que a chave {@code readOnly} devia cobrir: uma
 *       regeneração que reescreve manifesto e {@code .java} juntos a partir de uma definição
 *       desatualizada. Nesse caso os dois ficheiros continuam coerentes um com o outro, este
 *       teste fica verde, e os campos desaparecem na mesma. A prova negativa desse limite está
 *       descrita e executada em {@code 110-02-SUMMARY.md} -- apagar {@code requestedDate} dos
 *       dois ficheiros ao mesmo tempo faz este teste passar;</li>
 *   <li>não é prevenção nenhuma: nada neste repositório impede uma regeneração de sobrescrever os
 *       dois ficheiros.</li>
 * </ul>
 */
class WorkflowInboxItemDtoManifestCoherenceTest {

    private static final String MANIFEST_PATH = ".igrpstudio/sigdi/dto/WorkflowInboxItemDTO.json";

    @Test
    void manifestAttributesAndJavaFieldsAreTheSameSetInBothDirections() throws IOException {
        Set<String> manifestNames = readManifestAttributeNames();
        Set<String> javaFieldNames = declaredFieldNames(WorkflowInboxItemDTO.class);

        Set<String> onlyInManifest = new HashSet<>(manifestNames);
        onlyInManifest.removeAll(javaFieldNames);

        Set<String> onlyInJava = new HashSet<>(javaFieldNames);
        onlyInJava.removeAll(manifestNames);

        if (!onlyInManifest.isEmpty() || !onlyInJava.isEmpty()) {
            fail("WorkflowInboxItemDTO.java diverge de .igrpstudio/sigdi/dto/WorkflowInboxItemDTO.json"
                    + " -- campos só no manifesto: " + onlyInManifest
                    + "; campos só no .java: " + onlyInJava);
        }
    }

    @Test
    void manifestDeclaresReadOnlyTrue() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(MANIFEST_PATH));

        assertTrue(root.has("readOnly") && root.get("readOnly").asBoolean(false),
                "\"readOnly\": true tem de estar declarado na raiz do manifesto. Isto NÃO é uma"
                        + " garantia de que a regeneração respeita o campo -- para DTOs, o gerador"
                        + " (references/addDTO.md) não tem regra de enforcement nenhuma, ao contrário"
                        + " dos enums (references/addEnum.md:44,57). É só uma declaração de intenção"
                        + " que passa a valer se o gerador for corrigido, e que não deve ser apagada"
                        + " por descuido.");
    }

    private Set<String> readManifestAttributeNames() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(MANIFEST_PATH));
        JsonNode attributes = root.get("attributes");
        return StreamSupport.stream(attributes.spliterator(), false)
                .map(attribute -> attribute.get("name").asText())
                .collect(Collectors.toSet());
    }

    private Set<String> declaredFieldNames(Class<?> type) {
        Set<String> names = new HashSet<>();
        for (Field field : type.getDeclaredFields()) {
            if (!field.isSynthetic()) {
                names.add(field.getName());
            }
        }
        return names;
    }
}
