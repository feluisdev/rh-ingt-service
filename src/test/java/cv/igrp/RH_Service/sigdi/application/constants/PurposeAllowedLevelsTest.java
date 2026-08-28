package cv.igrp.RH_Service.sigdi.application.constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Testes de {@link PurposeAllowedLevels}: as sete combinações reais de (purpose, type), medidas
 * a 2026-08-27 (119-CONTEXT.md) -- não doze, como quatro documentos de planeamento afirmavam
 * antes dessa correcção. Enumera os sete pares literalmente, para que um {@link Purpose} novo
 * sem decisão de níveis quebre este teste em vez de passar em silêncio.
 */
class PurposeAllowedLevelsTest {

    @Test
    void allowedLevelsDePaaBscObjectivesDevolveListaVazia() {
        assertTrue(PurposeAllowedLevels.allowedLevels(Purpose.PAA_BSC_OBJECTIVES).isEmpty());
    }

    @Test
    void allowedLevelsDePaaDevolveUnitLevelEIndividualLevelPorEstaOrdem() {
        assertEquals(List.of(PaaLevel.UNIT_LEVEL, PaaLevel.INDIVIDUAL_LEVEL),
                PurposeAllowedLevels.allowedLevels(Purpose.PAA));
    }

    @Test
    void allowedLevelsDeCadaUmaDasQuatroFinalidadesSiadapDevolveSoIndividualLevel() {
        assertEquals(List.of(PaaLevel.INDIVIDUAL_LEVEL), PurposeAllowedLevels.allowedLevels(Purpose.SIADAP));
        assertEquals(List.of(PaaLevel.INDIVIDUAL_LEVEL), PurposeAllowedLevels.allowedLevels(Purpose.SIADAP_INTERIM));
        assertEquals(List.of(PaaLevel.INDIVIDUAL_LEVEL), PurposeAllowedLevels.allowedLevels(Purpose.SIADAP_SELF_EVAL));
        assertEquals(List.of(PaaLevel.INDIVIDUAL_LEVEL), PurposeAllowedLevels.allowedLevels(Purpose.SIADAP_FINAL));
    }

    @Test
    void allValidCombinationsDevolveExactamenteSetePares() {
        List<Map.Entry<Purpose, PaaLevel>> combinations = PurposeAllowedLevels.allValidCombinations();

        assertEquals(7, combinations.size());

        // As sete combinações reais (119-CONTEXT.md): PAA_BSC_OBJECTIVES entra com o seu par
        // marcador (UNIT_LEVEL) -- é a combinação de despacho que a Task 3 testa como a
        // quarta finalidade "sem geração" -- mesmo não sendo uma escolha real de nível
        // (allowedLevels(PAA_BSC_OBJECTIVES) é vazio, ver teste acima). isValid() exclui
        // deliberadamente este par específico -- ver o teste seguinte.
        Set<Map.Entry<Purpose, PaaLevel>> expected = Set.of(
                Map.entry(Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL),
                Map.entry(Purpose.PAA, PaaLevel.UNIT_LEVEL),
                Map.entry(Purpose.PAA, PaaLevel.INDIVIDUAL_LEVEL),
                Map.entry(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL),
                Map.entry(Purpose.SIADAP_INTERIM, PaaLevel.INDIVIDUAL_LEVEL),
                Map.entry(Purpose.SIADAP_SELF_EVAL, PaaLevel.INDIVIDUAL_LEVEL),
                Map.entry(Purpose.SIADAP_FINAL, PaaLevel.INDIVIDUAL_LEVEL)
        );
        assertEquals(expected, Set.copyOf(combinations));
    }

    @Test
    void isValidEVerdadeiroParaSeisDasSeteCombinacoesEFalsoParaOsOutros() {
        assertTrue(PurposeAllowedLevels.isValid(Purpose.PAA, PaaLevel.UNIT_LEVEL));
        assertTrue(PurposeAllowedLevels.isValid(Purpose.PAA, PaaLevel.INDIVIDUAL_LEVEL));
        assertTrue(PurposeAllowedLevels.isValid(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL));
        assertTrue(PurposeAllowedLevels.isValid(Purpose.SIADAP_INTERIM, PaaLevel.INDIVIDUAL_LEVEL));
        assertTrue(PurposeAllowedLevels.isValid(Purpose.SIADAP_SELF_EVAL, PaaLevel.INDIVIDUAL_LEVEL));
        assertTrue(PurposeAllowedLevels.isValid(Purpose.SIADAP_FINAL, PaaLevel.INDIVIDUAL_LEVEL));

        assertFalse(PurposeAllowedLevels.isValid(Purpose.SIADAP, PaaLevel.UNIT_LEVEL));
        assertFalse(PurposeAllowedLevels.isValid(Purpose.SIADAP_INTERIM, PaaLevel.UNIT_LEVEL));
        assertFalse(PurposeAllowedLevels.isValid(Purpose.SIADAP_SELF_EVAL, PaaLevel.UNIT_LEVEL));
        assertFalse(PurposeAllowedLevels.isValid(Purpose.SIADAP_FINAL, PaaLevel.UNIT_LEVEL));
        assertFalse(PurposeAllowedLevels.isValid(Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL));
        assertFalse(PurposeAllowedLevels.isValid(Purpose.PAA_BSC_OBJECTIVES, PaaLevel.INDIVIDUAL_LEVEL));
    }

    @Test
    void paaBscObjectivesNaoTemNivelValidoMasOMarcadorEUnitLevel() {
        assertFalse(PurposeAllowedLevels.isValid(Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL));
        assertEquals(PaaLevel.UNIT_LEVEL, PurposeAllowedLevels.marker(Purpose.PAA_BSC_OBJECTIVES));
    }

    @Test
    void allowedLevelsDeNuloLancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PurposeAllowedLevels.allowedLevels(null));
    }
}
