package cv.igrp.RH_Service.sigdi.application.constants;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals(5, Purpose.SIADAP_FINAL.getPosition());
    }

    @Test
    void fromCodeOrThrowRejectsUnknownCode() {
        assertThrows(IgrpResponseStatusException.class, () -> Purpose.fromCodeOrThrow("bogus"));
    }
}
