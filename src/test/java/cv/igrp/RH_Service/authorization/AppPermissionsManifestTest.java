package cv.igrp.RH_Service.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cv.igrp.RH_Service.authorization.permission.AppPermissions;
import cv.igrp.framework.auth.generated.PermissionsRegistry;
import cv.igrp.framework.stereotype.IgrpPermission;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

/**
 * Fecha o buraco nomeado no javadoc de {@link AppPermissions}: "adding a permission in one place
 * and not the other fails silently". Compara, nos dois sentidos, o que {@link AppPermissions}
 * declara com o que {@code .igrpstudio/permissions.json} declara para a plataforma de gestão de
 * acessos -- hoje nada verifica essa correspondência, e o custo de a manter em passo, apontado no
 * {@code 120-CONTEXT.md} como razão para reutilizar uma permissão em vez de declarar uma nova,
 * deixa de ser pago por boa-fé.
 *
 * <p><strong>Desvio face ao plano, medido durante esta tarefa:</strong> o plano descrevia "ler as
 * anotações {@code @IgrpPermission} de {@code AppPermissions} por reflexão sobre os campos
 * declarados". Isso não é possível -- {@link IgrpPermission} tem
 * {@code @Retention(RetentionPolicy.SOURCE)} (confirmado por {@code javap -v} contra
 * {@code stereotype-0.2.0-beta.1.jar}: {@code RuntimeVisibleAnnotations} não inclui
 * {@code IgrpPermission}, e o {@code .class} compilado de {@code AppPermissions} não carrega
 * nenhuma anotação nos campos). A anotação é um artefacto puramente de compilação, consumido só
 * pelo {@code PermissionSourceGenerator}, e desaparece do bytecode. O lado Java desta comparação
 * usa, em vez disso, {@link PermissionsRegistry.Permission} -- o enum que esse mesmo processador
 * gera a partir das anotações (ver o javadoc de {@link AppPermissions}: "the constants below are
 * therefore the source of truth; the enum ... {@code is} derived"). Cada constante do enum
 * gerado tem o mesmo nome do campo em {@code AppPermissions} que a originou (confirmado no
 * ficheiro gerado, {@code target/generated-sources/annotations/.../PermissionsRegistry.java}),
 * o que permite ligar univocamente cada campo Java à entrada do manifesto sem depender da
 * anotação em runtime.
 *
 * <p><strong>Limite herdado, nomeado e não resolvido aqui:</strong> enquanto o {@code AUT-05} não
 * estiver fechado (a sincronização com a plataforma de gestão de acessos falha por falta de
 * credenciais), nenhuma das permissões declaradas aqui chega de facto à plataforma, e nenhum
 * chamador real as detém. Este teste garante só que as duas declarações -- os campos de
 * {@code AppPermissions.java} (via o enum gerado a partir deles) e
 * {@code .igrpstudio/permissions.json} -- não divergem entre si; não prova nada sobre o que está
 * concedido na plataforma real.
 */
class AppPermissionsManifestTest {

    private static final String MANIFEST_PATH = ".igrpstudio/permissions.json";

    @Test
    void everyGeneratedPermissionHasAManifestEntryAndViceVersa() throws IOException {
        Map<String, String> generatedPermissions = readGeneratedPermissions();
        Map<String, String> manifestPermissions = readManifestPermissions();

        Set<String> onlyInJava = new TreeSet<>(generatedPermissions.keySet());
        onlyInJava.removeAll(manifestPermissions.keySet());

        Set<String> onlyInManifest = new TreeSet<>(manifestPermissions.keySet());
        onlyInManifest.removeAll(generatedPermissions.keySet());

        if (!onlyInJava.isEmpty() || !onlyInManifest.isEmpty()) {
            fail("AppPermissions.java diverge de " + MANIFEST_PATH
                    + " -- permissões só em AppPermissions.java (faltam no manifesto): " + onlyInJava
                    + "; permissões só em " + MANIFEST_PATH + " (falta a constante Java): " + onlyInManifest);
        }
    }

    @Test
    void descriptionsMatchForEveryPermissionName() throws IOException {
        Map<String, String> generatedPermissions = readGeneratedPermissions();
        Map<String, String> manifestPermissions = readManifestPermissions();

        Set<String> commonNames = new TreeSet<>(generatedPermissions.keySet());
        commonNames.retainAll(manifestPermissions.keySet());

        for (String name : commonNames) {
            assertEquals(manifestPermissions.get(name), generatedPermissions.get(name),
                    "Descrição diverge para \"" + name + "\" entre AppPermissions.java e "
                            + MANIFEST_PATH);
        }
    }

    @Test
    void fieldValueMatchesTheGeneratedCodeOfTheSameName() throws IOException {
        for (Field field : declaredPermissionFields()) {
            String fieldValue = readStaticStringField(field);
            PermissionsRegistry.Permission generated;
            try {
                generated = PermissionsRegistry.Permission.valueOf(field.getName());
            } catch (IllegalArgumentException e) {
                fail("Não existe PermissionsRegistry.Permission." + field.getName()
                        + " -- o processador de anotações não gerou uma constante com o mesmo nome"
                        + " do campo em AppPermissions.java, sinal de que a anotação @IgrpPermission"
                        + " está em falta ou desalinhada nesse campo.");
                return;
            }
            assertEquals(generated.getCode(), fieldValue,
                    "O campo " + field.getName() + " tem valor \"" + fieldValue
                            + "\" mas PermissionsRegistry.Permission." + field.getName()
                            + " (gerado a partir da anotação @IgrpPermission que o acompanha)"
                            + " declara code=\"" + generated.getCode() + "\" -- @PreAuthorize e a"
                            + " sincronização com o manifesto deixam de falar da mesma coisa se"
                            + " estes dois divergirem.");
        }
    }

    @Test
    void thereAreExactlyEightOnBothSides() throws IOException {
        assertEquals(8, declaredPermissionFields().size(),
                "Esperava oito permissões declaradas em AppPermissions.java");
        assertEquals(8, PermissionsRegistry.Permission.values().length,
                "Esperava oito constantes geradas em PermissionsRegistry.Permission");
        assertEquals(8, readManifestPermissions().size(),
                "Esperava oito entradas em " + MANIFEST_PATH);
    }

    /**
     * O lado "Java" desta comparação: para cada campo declarado em {@link AppPermissions},
     * procura-se a constante gerada com o mesmo nome ({@link PermissionsRegistry.Permission}) e
     * lê-se o {@code code}/{@code description} dela -- não se lê a anotação directamente, pela
     * razão medida no javadoc da classe (retenção {@code SOURCE}).
     */
    private Map<String, String> readGeneratedPermissions() {
        Map<String, String> permissions = new HashMap<>();
        for (PermissionsRegistry.Permission permission : PermissionsRegistry.Permission.values()) {
            permissions.put(permission.getCode(), permission.getDescription());
        }
        return permissions;
    }

    private java.util.List<Field> declaredPermissionFields() {
        java.util.List<Field> fields = new java.util.ArrayList<>();
        for (Field field : AppPermissions.class.getDeclaredFields()) {
            if (field.getType() == String.class && Modifier.isStatic(field.getModifiers())
                    && !field.isSynthetic()) {
                fields.add(field);
            }
        }
        return fields;
    }

    private Map<String, String> readManifestPermissions() throws IOException {
        File manifestFile = new File(MANIFEST_PATH);
        if (!manifestFile.isFile()) {
            fail("Manifesto não encontrado em " + manifestFile.getAbsolutePath()
                    + " -- o teste falha em vez de passar por omissão.");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(manifestFile);

        Map<String, String> permissions = new HashMap<>();
        for (JsonNode entry : root) {
            permissions.put(entry.get("name").asText(), entry.get("description").asText());
        }
        return permissions;
    }

    private String readStaticStringField(Field field) {
        try {
            field.setAccessible(true);
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Não foi possível ler o campo " + field.getName(), e);
        }
    }
}
