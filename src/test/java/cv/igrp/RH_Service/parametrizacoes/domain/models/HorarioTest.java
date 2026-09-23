package cv.igrp.RH_Service.parametrizacoes.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * O horario (assiduidade, primeiro passo): so as invariantes que fazem de uma lista de blocos um
 * horario. Os limites legais esperam o diploma de desenvolvimento e nao se validam.
 */
class HorarioTest {

    private static BlocoHorario b(DayOfWeek dia, String inicio, String fim, boolean obrigatorio) {
        return new BlocoHorario(dia, LocalTime.parse(inicio), LocalTime.parse(fim), obrigatorio);
    }

    /** Segunda a sexta, 08:00-12:30 e 14:00-17:30: 8h por dia, 40h por semana. */
    private static List<BlocoHorario> semanaNormal() {
        List<BlocoHorario> blocos = new ArrayList<>();
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            blocos.add(b(d, "08:00", "12:30", false));
            blocos.add(b(d, "14:00", "17:30", false));
        }
        return blocos;
    }

    private static void assert422(Runnable r) {
        var e = assertThrows(IgrpResponseStatusException.class, r::run);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getStatusCode().value());
    }

    private static void assert409(Runnable r) {
        var e = assertThrows(IgrpResponseStatusException.class, r::run);
        assertEquals(HttpStatus.CONFLICT.value(), e.getStatusCode().value());
    }

    @Test
    void fixoCalculaAsHorasDosBlocosETodosSaoObrigatorios() {
        var h = Horario.criar("Rígido", ControloHorario.FIXO, null, null, semanaNormal());

        assertEquals(8 * 60, h.minutosNoDia(DayOfWeek.MONDAY));
        assertEquals(0, h.minutosNoDia(DayOfWeek.SATURDAY));
        assertEquals(40 * 60, h.minutosSemanais());
        assertTrue(h.getBlocos().stream().allMatch(BlocoHorario::obrigatorio));
        assertTrue(h.isActive());
        assertFalse(h.isBase());
    }

    @Test
    void flexivelContaADuracaoDiariaNosDiasComBlocos() {
        var blocos = List.of(
                b(DayOfWeek.MONDAY, "07:30", "10:00", false),
                b(DayOfWeek.MONDAY, "10:00", "12:00", true),
                b(DayOfWeek.MONDAY, "14:30", "16:30", true),
                b(DayOfWeek.MONDAY, "16:30", "19:30", false),
                b(DayOfWeek.TUESDAY, "08:00", "18:00", false));
        var h = Horario.criar("Flexível", ControloHorario.FLEXIVEL, PeriodoAfericao.MES, 7 * 60, blocos);

        assertEquals(7 * 60, h.minutosNoDia(DayOfWeek.MONDAY));
        assertEquals(14 * 60, h.minutosSemanais());
        assertFalse(h.getBlocos().stream().allMatch(BlocoHorario::obrigatorio));
    }

    @Test
    void blocosSobrepostosOuAoContrarioSao422() {
        assert422(() -> Horario.criar("X", ControloHorario.FIXO, null, null, List.of(
                b(DayOfWeek.MONDAY, "08:00", "12:30", true), b(DayOfWeek.MONDAY, "12:00", "17:00", true))));
        assert422(() -> Horario.criar("X", ControloHorario.FIXO, null, null, List.of(
                b(DayOfWeek.MONDAY, "17:00", "08:00", true))));
        assert422(() -> Horario.criar("X", ControloHorario.FIXO, null, null, List.of()));
        assert422(() -> Horario.criar(" ", ControloHorario.FIXO, null, null, semanaNormal()));
        assert422(() -> Horario.criar("X", null, null, null, semanaNormal()));
    }

    @Test
    void blocosEncostadosNaoSeSobrepoem() {
        var h = Horario.criar("X", ControloHorario.FIXO, null, null, List.of(
                b(DayOfWeek.MONDAY, "08:00", "12:00", true), b(DayOfWeek.MONDAY, "12:00", "13:00", true)));
        assertEquals(5 * 60, h.minutosSemanais());
    }

    @Test
    void periodoEDuracaoSoNoFlexivel() {
        assert422(() -> Horario.criar("X", ControloHorario.FIXO, PeriodoAfericao.SEMANA, null, semanaNormal()));
        assert422(() -> Horario.criar("X", ControloHorario.FIXO, null, 420, semanaNormal()));
        assert422(() -> Horario.criar("X", ControloHorario.FLEXIVEL, null, 420, semanaNormal()));
        assert422(() -> Horario.criar("X", ControloHorario.FLEXIVEL, PeriodoAfericao.SEMANA, null, semanaNormal()));
    }

    @Test
    void duracaoQueNaoCabeOuPlataformasQuePassamSao422() {
        assert422(() -> Horario.criar("X", ControloHorario.FLEXIVEL, PeriodoAfericao.SEMANA, 9 * 60, semanaNormal()));
        assert422(() -> Horario.criar("X", ControloHorario.FLEXIVEL, PeriodoAfericao.SEMANA, 2 * 60, List.of(
                b(DayOfWeek.MONDAY, "08:00", "12:00", true))));
    }

    @Test
    void oBaseNaoSeDesactivaEUmInactivoNaoPodeSerBase() {
        var h = Horario.criar("Base", ControloHorario.FIXO, null, null, semanaNormal());
        h.marcarComoBase();
        assert409(h::desativar);

        h.desmarcarBase();
        h.desativar();
        assert422(h::marcarComoBase);
    }
}
