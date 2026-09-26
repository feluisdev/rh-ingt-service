package cv.igrp.RH_Service.colaboradores.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.models.EspecieProcessoDisciplinar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/** BR-DIS-25: 5 seguidos / 8 interpolados → falta de assiduidade; 12 / 15 / 25 em 24 meses → abandono de lugar. */
class RegrasAssiduidadeDisciplinarTest {

    private static final Predicate<LocalDate> UTIL = d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY;
    /** Segunda-feira. */
    private static final LocalDate SEG = LocalDate.of(2026, 3, 2);

    private static TreeSet<LocalDate> uteis(LocalDate de, int n) {
        var s = new TreeSet<LocalDate>();
        LocalDate d = de;
        while (s.size() < n) {
            if (UTIL.test(d)) s.add(d);
            d = d.plusDays(1);
        }
        return s;
    }

    @Test
    void quatroSeguidosNaoChegamCincoSimMesmoAtravessandoOFimDeSemana() {
        assertTrue(RegrasAssiduidadeDisciplinar.avaliar(uteis(SEG, 4), SEG.plusDays(10), UTIL).isEmpty());
        // Quinta, sexta, segunda, terça, quarta: 5 úteis seguidos.
        var s = RegrasAssiduidadeDisciplinar.avaliar(uteis(SEG.plusDays(3), 5), SEG.plusDays(10), UTIL).orElseThrow();
        assertEquals(EspecieProcessoDisciplinar.FALTA_ASSIDUIDADE, s.especie());
        assertEquals(5, s.seguidos());
    }

    @Test
    void oitoInterpoladosNoAno() {
        var f = new TreeSet<LocalDate>();
        for (int i = 0; i < 8; i++) f.add(SEG.plusWeeks(i));
        var s = RegrasAssiduidadeDisciplinar.avaliar(f, SEG.plusWeeks(9), UTIL).orElseThrow();
        assertEquals(EspecieProcessoDisciplinar.FALTA_ASSIDUIDADE, s.especie());
        assertEquals(1, s.seguidos());
        // As do ano anterior não contam para o ano civil.
        var velhas = new TreeSet<LocalDate>();
        for (int i = 0; i < 8; i++) velhas.add(SEG.minusYears(1).plusWeeks(i));
        assertTrue(RegrasAssiduidadeDisciplinar.avaliar(velhas, SEG, UTIL).isEmpty());
    }

    @Test
    void abandonoDeLugarConsomeAAssiduidade() {
        var s = RegrasAssiduidadeDisciplinar.avaliar(uteis(SEG, 12), SEG.plusDays(30), UTIL).orElseThrow();
        assertEquals(EspecieProcessoDisciplinar.ABANDONO_LUGAR, s.especie());
        var em24 = new TreeSet<LocalDate>();
        for (int i = 0; i < 18; i++) em24.add(SEG.minusMonths(12).plusWeeks(i));
        for (int i = 0; i < 7; i++) em24.add(SEG.plusWeeks(i));
        var t = RegrasAssiduidadeDisciplinar.avaliar(em24, SEG.plusWeeks(8), UTIL).orElseThrow();
        assertEquals(EspecieProcessoDisciplinar.ABANDONO_LUGAR, t.especie());
        assertEquals(25, t.em24Meses());
    }
}
