package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/** O dia calcula-se das marcacoes validas: pares entrada/saida; o que nao emparelha e anomalia, e nao conta. */
class DiaAssiduidadeTest {

    private static final LocalDate DIA = LocalDate.of(2026, 9, 21);
    private final FuncionarioId funcionario = FuncionarioId.gerarNovo();

    private MarcacaoAssiduidade m(String hora, SentidoMarcacao sentido) {
        return m(DIA, hora, sentido, false);
    }

    private MarcacaoAssiduidade m(LocalDate dia, String hora, SentidoMarcacao sentido, boolean anulada) {
        return MarcacaoAssiduidade.reconstruir(MarcacaoAssiduidadeId.gerarNovo(), funcionario,
                LocalDateTime.of(dia, LocalTime.parse(hora)), sentido, OrigemMarcacao.IMPORTADO, null, null,
                anulada, anulada ? "erro" : null, null);
    }

    @Test
    void doisPeriodosComIntervalo() {
        var dia = DiaAssiduidade.calcular(DIA, List.of(
                m("14:00", SentidoMarcacao.ENTRADA), m("08:00", SentidoMarcacao.ENTRADA),
                m("12:30", SentidoMarcacao.SAIDA), m("17:30", SentidoMarcacao.SAIDA)));

        assertEquals(2, dia.periodos().size());
        assertEquals(LocalTime.of(8, 0), dia.periodos().get(0).entrada());
        assertEquals(List.of(90), dia.intervalosMinutos());
        assertEquals(8 * 60, dia.minutosTrabalhados());
        assertTrue(dia.anomalias().isEmpty());
    }

    @Test
    void entradaSemSaidaNaoConta() {
        var dia = DiaAssiduidade.calcular(DIA, List.of(
                m("08:00", SentidoMarcacao.ENTRADA), m("12:00", SentidoMarcacao.SAIDA), m("13:00", SentidoMarcacao.ENTRADA)));

        assertEquals(4 * 60, dia.minutosTrabalhados());
        assertEquals(Set.of(AnomaliaMarcacao.ENTRADA_SEM_SAIDA), dia.anomalias());
    }

    @Test
    void saidaSemEntradaEEntradasSeguidas() {
        var dia = DiaAssiduidade.calcular(DIA, List.of(
                m("07:00", SentidoMarcacao.SAIDA), m("08:00", SentidoMarcacao.ENTRADA),
                m("08:10", SentidoMarcacao.ENTRADA), m("12:10", SentidoMarcacao.SAIDA)));

        // Conta a segunda entrada: das 08:10 as 12:10.
        assertEquals(4 * 60, dia.minutosTrabalhados());
        assertEquals(Set.of(AnomaliaMarcacao.SAIDA_SEM_ENTRADA, AnomaliaMarcacao.ENTRADAS_SEGUIDAS), dia.anomalias());
    }

    @Test
    void anuladasEOutrosDiasIgnoram_se() {
        var dia = DiaAssiduidade.calcular(DIA, List.of(
                m("08:00", SentidoMarcacao.ENTRADA),
                m(DIA, "09:00", SentidoMarcacao.SAIDA, true),
                m(DIA.plusDays(1), "10:00", SentidoMarcacao.SAIDA, false),
                m("16:00", SentidoMarcacao.SAIDA)));

        assertEquals(8 * 60, dia.minutosTrabalhados());
        assertTrue(dia.anomalias().isEmpty());
    }

    @Test
    void semMarcacoesNaoHaNada() {
        var dia = DiaAssiduidade.calcular(DIA, List.of());
        assertEquals(0, dia.minutosTrabalhados());
        assertTrue(dia.periodos().isEmpty());
        assertTrue(dia.anomalias().isEmpty());
    }
}
