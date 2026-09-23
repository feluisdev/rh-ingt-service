package cv.igrp.RH_Service.colaboradores.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * DL n.º 3/2010, art. 76.º (V56): os fins-de-semana e feriados <b>intercalados</b> numa sucessão
 * de faltas contam, salvo se a lei disser dias úteis. Setembro de 2026: dia 18 é sexta.
 */
class ContagemDeDiasTest {

    private final DiasUteisCalculator calc = new DiasUteisCalculator();

    private int seguidos(String inicio, String fim, Set<LocalDate> feriados) {
        return calc.calcular(LocalDate.parse(inicio), LocalDate.parse(fim), feriados, ContagemDias.DIAS_SEGUIDOS);
    }

    private int uteis(String inicio, String fim) {
        return calc.calcular(LocalDate.parse(inicio), LocalDate.parse(fim), Set.of(), ContagemDias.DIAS_UTEIS);
    }

    @Test
    void deSextaASegundaOFimDeSemanaIntercaladoConta() {
        assertEquals(4, seguidos("2026-09-18", "2026-09-21", Set.of()));
        assertEquals(2, uteis("2026-09-18", "2026-09-21"));
    }

    @Test
    void oFimDeSemanaDaPontaNaoEstaNoDecursoDaFalta() {
        assertEquals(1, seguidos("2026-09-18", "2026-09-20", Set.of()));
        assertEquals(1, seguidos("2026-09-19", "2026-09-21", Set.of()));
    }

    @Test
    void feriadoIntercaladoContaEFeriadoNaPontaNao() {
        var quarta = LocalDate.of(2026, 9, 16);
        assertEquals(3, seguidos("2026-09-15", "2026-09-17", Set.of(quarta)));
        assertEquals(1, seguidos("2026-09-15", "2026-09-16", Set.of(quarta)));
    }

    @Test
    void seminarioDeQuintaAQuartaSaoSeteDiasSeguidos_eOTectoDeCincoApanhaOs() {
        // Art. 21.º n.º 2: «não pode ser superior a 5 dias consecutivos». Em dias úteis eram 5 e
        // passavam; em dias seguidos são 7.
        assertEquals(7, seguidos("2026-09-17", "2026-09-23", Set.of()));
        assertEquals(5, uteis("2026-09-17", "2026-09-23"));
    }

    @Test
    void lutoDeOitoDiasAComecarNumaSexta() {
        // Art. 15.º n.º 1 al. b): até 8. Sexta 18 a sexta 25 são 8 dias seguidos.
        assertEquals(8, seguidos("2026-09-18", "2026-09-25", Set.of()));
    }

    @Test
    void soFimDeSemanaE422NosDoisModos() {
        assertThrows(IgrpResponseStatusException.class, () -> seguidos("2026-09-19", "2026-09-20", Set.of()));
        assertThrows(IgrpResponseStatusException.class, () -> uteis("2026-09-19", "2026-09-20"));
    }

    @Test
    void semContagemDitaContaEmDiasUteis_comoSempre() {
        assertEquals(2, calc.calcular(LocalDate.parse("2026-09-18"), LocalDate.parse("2026-09-21"), Set.of(), null));
    }
}
