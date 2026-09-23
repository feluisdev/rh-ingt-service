package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Onde um feriado cai dentro de um período (V55). O defeito que isto fecha: os feriados tinham
 * data de um ano só, e a 1 de Janeiro de 2027 a aplicação deixava de conhecer feriado nenhum.
 */
class FeriadoTest {

    private static List<LocalDate> entre(Feriado f, String inicio, String fim) {
        return f.ocorrenciasEntre(LocalDate.parse(inicio), LocalDate.parse(fim)).toList();
    }

    @Test
    void pontualSoCaiNaSuaData() {
        var sextaSanta = new Feriado(LocalDate.of(2026, 4, 3), false);

        assertEquals(List.of(LocalDate.of(2026, 4, 3)), entre(sextaSanta, "2026-04-01", "2026-04-10"));
        assertEquals(List.of(), entre(sextaSanta, "2027-03-01", "2027-04-30"));
    }

    @Test
    void recorrenteCaiTodosOsAnos_eEsteEraODefeito() {
        var independencia = new Feriado(LocalDate.of(2026, 7, 5), true);

        assertEquals(List.of(LocalDate.of(2027, 7, 5)), entre(independencia, "2027-07-01", "2027-07-31"));
        assertEquals(List.of(LocalDate.of(2031, 7, 5)), entre(independencia, "2031-07-05", "2031-07-05"));
    }

    @Test
    void recorrenteNaoCaiAntesDoAnoEmQueComecou() {
        var feriadoNovo = new Feriado(LocalDate.of(2026, 3, 10), true);

        assertEquals(List.of(), entre(feriadoNovo, "2025-03-01", "2025-03-31"));
    }

    @Test
    void periodoQueAtravessaOAnoApanhaOsDoisAnos() {
        var anoNovo = new Feriado(LocalDate.of(2026, 1, 1), true);

        assertEquals(List.of(LocalDate.of(2027, 1, 1)), entre(anoNovo, "2026-12-28", "2027-01-05"));
        assertEquals(List.of(LocalDate.of(2027, 1, 1), LocalDate.of(2028, 1, 1)),
                entre(anoNovo, "2026-06-01", "2028-06-01"));
    }

    @Test
    void periodoInvertidoNaoTemOcorrencias() {
        assertEquals(List.of(), entre(new Feriado(LocalDate.of(2026, 1, 1), true), "2027-01-05", "2026-12-28"));
    }

    @Test
    void vinteENoveDeFevereiroRecorrenteSoCaiNosBissextos() {
        var bissexto = new Feriado(LocalDate.of(2024, 2, 29), true);

        assertEquals(List.of(LocalDate.of(2028, 2, 29)), entre(bissexto, "2025-01-01", "2028-12-31"));
    }
}
