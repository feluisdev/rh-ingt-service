package cv.igrp.RH_Service.parametrizacoes.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** As invariantes do feriado (V55): recorrência e área geográfica. */
class PublicHolidayTest {

    @Test
    void nacionalRecorrenteSemArea() {
        var natal = PublicHoliday.criar("Natal", LocalDate.of(2026, 12, 25), true, null, true, null);

        assertTrue(natal.isRecurring());
        assertNull(natal.getAreaCkey());
        assertTrue(natal.isActive());
    }

    @Test
    void municipalComArea() {
        var sVicente = PublicHoliday.criar("Dia do Município", LocalDate.of(2026, 1, 22), false, null, true, " MINDELO ");

        assertEquals("MINDELO", sVicente.getAreaCkey());
    }

    @Test
    void areaEmBrancoEAusenciaDeArea() {
        var f = PublicHoliday.criar("X", LocalDate.of(2026, 3, 1), false, null, false, "   ");

        assertNull(f.getAreaCkey());
    }

    @Test
    void nacionalComAreaE422() {
        var e = assertThrows(IgrpResponseStatusException.class,
                () -> PublicHoliday.criar("Natal", LocalDate.of(2026, 12, 25), true, null, true, "MINDELO"));

        assertEquals(422, e.getBody().getStatus());
    }

    @Test
    void vinteENoveDeFevereiroRecorrenteE422() {
        var e = assertThrows(IgrpResponseStatusException.class,
                () -> PublicHoliday.criar("X", LocalDate.of(2028, 2, 29), false, null, true, null));

        assertEquals(422, e.getBody().getStatus());
    }

    @Test
    void vinteENoveDeFevereiroPontualPassa() {
        var f = PublicHoliday.criar("X", LocalDate.of(2028, 2, 29), false, null, false, null);

        assertEquals(LocalDate.of(2028, 2, 29), f.getHolidayDate());
    }

    @Test
    void atualizarValidaEOmitirLimpa() {
        var f = PublicHoliday.criar("X", LocalDate.of(2026, 1, 22), false, null, true, "MINDELO");

        assertThrows(IgrpResponseStatusException.class,
                () -> f.atualizar("X", LocalDate.of(2026, 1, 22), true, null, true, "MINDELO"));
        assertEquals("MINDELO", f.getAreaCkey());

        f.atualizar("X", LocalDate.of(2026, 1, 22), false, null, false, null);
        assertNull(f.getAreaCkey());
        assertEquals(false, f.isRecurring());
    }
}
