package cv.igrp.RH_Service.sigdi.application.constants;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurposeTest {

    @Test
    void fromCodeOrThrowResolvesPaaBscObjectives() {
        assertEquals(Purpose.PAA_BSC_OBJECTIVES, Purpose.fromCodeOrThrow("PAA_BSC_OBJECTIVES"));
    }

    @Test
    void fromCodeOrThrowResolvesSiadapInterim() {
        assertEquals(Purpose.SIADAP_INTERIM, Purpose.fromCodeOrThrow("SIADAP_INTERIM"));
    }

    @Test
    void fromCodeOrThrowResolvesSiadapFinal() {
        assertEquals(Purpose.SIADAP_FINAL, Purpose.fromCodeOrThrow("SIADAP_FINAL"));
    }

    @Test
    void fromCodeOrThrowRegressionStillResolvesPaaAndSiadap() {
        assertEquals(Purpose.PAA, Purpose.fromCodeOrThrow("PAA"));
        assertEquals(Purpose.SIADAP, Purpose.fromCodeOrThrow("SIADAP"));
    }

    @Test
    void paaCodeAndDescriptionAreUnchanged() {
        assertEquals("PAA", Purpose.PAA.getCode());
        assertEquals("Plano de Atividades Anual", Purpose.PAA.getDescription());
    }

    @Test
    void siadapCodeAndDescriptionAreUnchanged() {
        assertEquals("SIADAP", Purpose.SIADAP.getCode());
        assertEquals("Avaliação de Desempenho (SIADAP)", Purpose.SIADAP.getDescription());
    }

    @Test
    void getPositionReturnsFixedAnnualSequenceOrder() {
        assertEquals(1, Purpose.PAA_BSC_OBJECTIVES.getPosition());
        assertEquals(2, Purpose.PAA.getPosition());
        assertEquals(3, Purpose.SIADAP.getPosition());
        assertEquals(4, Purpose.SIADAP_INTERIM.getPosition());
        assertEquals(5, Purpose.SIADAP_SELF_EVAL.getPosition());
        assertEquals(6, Purpose.SIADAP_FINAL.getPosition());
    }

    @Test
    void fromCodeOrThrowRejectsUnknownCode() {
        assertThrows(IgrpResponseStatusException.class, () -> Purpose.fromCodeOrThrow("bogus"));
    }

    @Test
    void fromCodeOrThrowResolvesSiadapSelfEval() {
        assertEquals(Purpose.SIADAP_SELF_EVAL, Purpose.fromCodeOrThrow("SIADAP_SELF_EVAL"));
    }

    @Test
    void siadapSelfEvalCodeAndDescriptionAreUnchanged() {
        assertEquals("SIADAP_SELF_EVAL", Purpose.SIADAP_SELF_EVAL.getCode());
        assertEquals("Autoavaliação SIADAP", Purpose.SIADAP_SELF_EVAL.getDescription());
    }

    @Test
    void everyPurposeCodeFitsThePersistedColumn() {
        // The `purpose` column is VARCHAR(20) with no CHECK constraint
        // (V19__paa_submission_period_purpose.sql:12), so this limit must hold for
        // every constant or the first write with a longer code fails at runtime
        // instead of at build time.
        for (Purpose purpose : Purpose.values()) {
            assertTrue(purpose.getCode().length() <= 20,
                    () -> purpose.getCode() + " exceeds the persisted column's 20-character limit");
        }
    }

    @Test
    void positionsFormAContiguousSequenceWithoutDuplicates() {
        Set<Integer> expected = IntStream.rangeClosed(1, Purpose.values().length)
                .boxed()
                .collect(Collectors.toSet());
        Set<Integer> actual = Arrays.stream(Purpose.values())
                .map(Purpose::getPosition)
                .collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
