package cv.igrp.RH_Service.sigdi.application.constants;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * As sete combinações reais de {@link Purpose} e {@link PaaLevel} (Fase 119, medido a
 * 2026-08-27, 119-CONTEXT.md). <strong>São sete, não doze</strong> -- o número doze (seis
 * valores de {@code Purpose} vezes dois de {@code PaaLevel}) estava repetido em quatro
 * documentos de planeamento (REQUIREMENTS.md, PROJECT.md, ROADMAP.md, STATE.md) e era erro,
 * corrigido nessa data. Nem toda combinação de {@code Purpose} × {@code PaaLevel} é válida:
 * {@code PAA} admite os dois níveis, as quatro finalidades SIADAP (SIADAP, SIADAP_INTERIM,
 * SIADAP_SELF_EVAL, SIADAP_FINAL) admitem só {@code INDIVIDUAL_LEVEL}, e
 * {@code PAA_BSC_OBJECTIVES} não tem conceito de nível nenhum -- {@link #allowedLevels} devolve
 * lista vazia para essa finalidade.
 *
 * <p><strong>A sétima combinação é o par-marcador de {@code PAA_BSC_OBJECTIVES}.</strong>
 * {@link PaaSubmissionPeriod} exige sempre um {@code type} não nulo, mesmo para uma finalidade
 * sem conceito de nível -- o frontend ({@code PURPOSE_ALLOWED_LEVELS} de
 * {@code src/app/(myapp)/types/tactical.ts}) submete {@code UNIT_LEVEL} como marcador nesse
 * caso. {@link #allValidCombinations()} inclui esse par marcador (contando as sete combinações
 * de despacho que a Fase 119 trata, uma por finalidade além das duas de {@code PAA}), mas
 * {@link #isValid(Purpose, PaaLevel)} devolve deliberadamente {@code false} para esse par
 * específico -- {@code PAA_BSC_OBJECTIVES} nunca teve uma escolha real de nível para validar, só
 * um valor técnico obrigatório pela forma do agregado. Um {@code isValid} que aceitasse o
 * marcador como "válido" confundiria a ausência de nível com uma escolha genuína.
 *
 * <p><strong>Esta regra existia até hoje só no frontend</strong>, em
 * {@code PURPOSE_ALLOWED_LEVELS} (`src/app/(myapp)/types/tactical.ts`), e não no backend. Este
 * ficheiro é a cópia da mesma regra do lado do servidor, <strong>com sincronização por nenhuma
 * ferramenta</strong> -- quem alterar um tem de alterar o outro.
 */
public final class PurposeAllowedLevels {

    private static final Map<Purpose, List<PaaLevel>> ALLOWED_LEVELS = buildAllowedLevels();

    private PurposeAllowedLevels() {
    }

    private static Map<Purpose, List<PaaLevel>> buildAllowedLevels() {
        Map<Purpose, List<PaaLevel>> map = new EnumMap<>(Purpose.class);
        map.put(Purpose.PAA_BSC_OBJECTIVES, List.of());
        map.put(Purpose.PAA, List.of(PaaLevel.UNIT_LEVEL, PaaLevel.INDIVIDUAL_LEVEL));
        map.put(Purpose.SIADAP, List.of(PaaLevel.INDIVIDUAL_LEVEL));
        map.put(Purpose.SIADAP_INTERIM, List.of(PaaLevel.INDIVIDUAL_LEVEL));
        map.put(Purpose.SIADAP_SELF_EVAL, List.of(PaaLevel.INDIVIDUAL_LEVEL));
        map.put(Purpose.SIADAP_FINAL, List.of(PaaLevel.INDIVIDUAL_LEVEL));
        return Collections.unmodifiableMap(map);
    }

    /**
     * Os níveis que {@code purpose} admite como escolha real. Vazia para
     * {@code PAA_BSC_OBJECTIVES}, que não tem conceito de nível -- ver {@link #marker(Purpose)}
     * para o valor técnico que esse caso submete.
     *
     * @throws IllegalArgumentException se {@code purpose} for {@code null}
     */
    public static List<PaaLevel> allowedLevels(Purpose purpose) {
        if (purpose == null) {
            throw new IllegalArgumentException("purpose não pode ser nulo");
        }
        return ALLOWED_LEVELS.get(purpose);
    }

    /**
     * {@code true} se {@code (purpose, type)} for uma escolha real e válida. Devolve
     * {@code false} para {@code (PAA_BSC_OBJECTIVES, UNIT_LEVEL)} mesmo esse par aparecendo em
     * {@link #allValidCombinations()} -- ver o Javadoc de classe.
     */
    public static boolean isValid(Purpose purpose, PaaLevel type) {
        if (purpose == null || type == null) {
            return false;
        }
        return ALLOWED_LEVELS.get(purpose).contains(type);
    }

    /**
     * As sete combinações de despacho da Fase 119: os seis pares reais de
     * {@link #allowedLevels} mais o par-marcador de {@code PAA_BSC_OBJECTIVES}
     * ({@code PAA_BSC_OBJECTIVES}, {@link #marker}). Cada uma das seis finalidades de
     * {@link Purpose} contribui pelo menos uma entrada -- se uma finalidade nova for
     * acrescentada a {@link Purpose} sem decisão explícita de níveis aqui, este método não
     * falha sozinho; é o teste {@code PurposeAllowedLevelsTest} que a enumera literalmente que
     * apanha a omissão.
     */
    public static List<Map.Entry<Purpose, PaaLevel>> allValidCombinations() {
        List<Map.Entry<Purpose, PaaLevel>> combinations = new ArrayList<>();
        for (Purpose purpose : Purpose.values()) {
            List<PaaLevel> levels = ALLOWED_LEVELS.get(purpose);
            if (levels.isEmpty()) {
                combinations.add(new AbstractMap.SimpleImmutableEntry<>(purpose, marker(purpose)));
            } else {
                for (PaaLevel level : levels) {
                    combinations.add(new AbstractMap.SimpleImmutableEntry<>(purpose, level));
                }
            }
        }
        return Collections.unmodifiableList(combinations);
    }

    /**
     * O valor técnico que o frontend submete para {@code type} quando {@code purpose} não tem
     * conceito de nível ({@link #allowedLevels} vazia). Hoje só {@code PAA_BSC_OBJECTIVES} está
     * nesse caso, e o marcador é sempre {@link PaaLevel#UNIT_LEVEL}.
     */
    public static PaaLevel marker(Purpose purpose) {
        if (purpose == null) {
            throw new IllegalArgumentException("purpose não pode ser nulo");
        }
        return PaaLevel.UNIT_LEVEL;
    }
}
