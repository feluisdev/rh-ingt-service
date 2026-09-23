package cv.igrp.RH_Service.colaboradores.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cv.igrp.RH_Service.colaboradores.domain.models.DiaAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoDiaApurado;
import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.MotivoFalta;
import cv.igrp.RH_Service.colaboradores.domain.models.OrigemMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.models.SentidoMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PeriodoAfericao;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * DL n.o 3/2010, art. 13.o: o tempo em falta de cada dia, o debito do flexivel no fim da afericao, e a
 * conversao do n.o 4 (o parcial soma-se no mes; o resto ate meio periodo e meia falta, acima e uma).
 */
class ApuramentoFaltasTest {

    private static final LocalDate SEGUNDA = LocalDate.of(2026, 9, 7);
    private final FuncionarioId funcionario = FuncionarioId.gerarNovo();

    /** 08:00-12:00 e 13:00-17:00, segunda a sexta: 8h. */
    private static Horario fixo() {
        List<BlocoHorario> blocos = new ArrayList<>();
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            blocos.add(new BlocoHorario(d, LocalTime.of(8, 0), LocalTime.of(12, 0), true));
            blocos.add(new BlocoHorario(d, LocalTime.of(13, 0), LocalTime.of(17, 0), true));
        }
        return Horario.criar("Fixo", ControloHorario.FIXO, null, null, blocos);
    }

    /** Margens 07:00-10:00 e 16:00-19:00, plataformas 10:00-12:00 e 14:00-16:00; 7h por dia, aferido por semana. */
    private static Horario flexivel() {
        List<BlocoHorario> blocos = new ArrayList<>();
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            blocos.add(new BlocoHorario(d, LocalTime.of(7, 0), LocalTime.of(10, 0), false));
            blocos.add(new BlocoHorario(d, LocalTime.of(10, 0), LocalTime.of(12, 0), true));
            blocos.add(new BlocoHorario(d, LocalTime.of(14, 0), LocalTime.of(16, 0), true));
            blocos.add(new BlocoHorario(d, LocalTime.of(16, 0), LocalTime.of(19, 0), false));
        }
        return Horario.criar("Flex", ControloHorario.FLEXIVEL, PeriodoAfericao.SEMANA, 7 * 60, blocos);
    }

    private ApuramentoFaltas.Dia dia(LocalDate data, Horario h, String... horas) {
        List<MarcacaoAssiduidade> ms = new ArrayList<>();
        for (int i = 0; i < horas.length; i++)
            ms.add(MarcacaoAssiduidade.reconstruir(MarcacaoAssiduidadeId.gerarNovo(), funcionario,
                    LocalDateTime.of(data, LocalTime.parse(horas[i])), i % 2 == 0 ? SentidoMarcacao.ENTRADA : SentidoMarcacao.SAIDA,
                    OrigemMarcacao.IMPORTADO, null, null, false, null, null));
        return new ApuramentoFaltas.Dia(data, null, h, DiaAssiduidade.calcular(data, ms), !ms.isEmpty());
    }

    @Test
    void fixoCumpridoNaoTemFalta() {
        var r = ApuramentoFaltas.apurar(List.of(dia(SEGUNDA, fixo(), "07:55", "12:05", "12:58", "17:10")));
        assertEquals(EstadoDiaApurado.SEM_FALTA, r.dias().get(0).estado());
        assertEquals(0, r.minutosParciais());
    }

    @Test
    void atrasoESaidaAntecipadaContamNoFixo() {
        var r = ApuramentoFaltas.apurar(List.of(dia(SEGUNDA, fixo(), "08:30", "12:00", "13:00", "16:00")));
        var d = r.dias().get(0);
        assertEquals(EstadoDiaApurado.COM_FALTA, d.estado());
        assertEquals(MotivoFalta.INCOMPLETO, d.motivo());
        assertEquals(90, d.minutosEmFalta());
    }

    @Test
    void semNenhumaMarcacaoEODiaInteiro() {
        var r = ApuramentoFaltas.apurar(List.of(dia(SEGUNDA, fixo())));
        assertEquals(MotivoFalta.SEM_REGISTO, r.dias().get(0).motivo());
        assertEquals(480, r.dias().get(0).minutosEmFalta());
        assertEquals(1, r.diasSemRegisto());
        assertEquals(new BigDecimal("1"), r.totalFaltas());
    }

    @Test
    void comAnomaliaFicaPorCorrigirENaoConta() {
        var r = ApuramentoFaltas.apurar(List.of(dia(SEGUNDA, fixo(), "08:00")));
        assertEquals(EstadoDiaApurado.POR_CORRIGIR, r.dias().get(0).estado());
        assertEquals(0, r.dias().get(0).minutosEmFalta());
        assertEquals(1, r.diasPorCorrigir());
    }

    @Test
    void sabadoEDescansoEOEstadoPrevioPassaAoLado() {
        var sabado = new ApuramentoFaltas.Dia(SEGUNDA.plusDays(5), null, fixo(), DiaAssiduidade.calcular(SEGUNDA.plusDays(5), List.of()), false);
        var feriado = new ApuramentoFaltas.Dia(SEGUNDA, EstadoDiaApurado.FERIADO, null, DiaAssiduidade.calcular(SEGUNDA, List.of()), false);
        var r = ApuramentoFaltas.apurar(List.of(sabado, feriado));
        assertEquals(EstadoDiaApurado.DESCANSO, r.dias().get(0).estado());
        assertEquals(EstadoDiaApurado.FERIADO, r.dias().get(1).estado());
        assertNull(r.dias().get(1).motivo());
    }

    @Test
    void flexivelPlataformaDescobertaEDebitoDaSemanaSemContarDuasVezes() {
        var h = flexivel();
        var r = ApuramentoFaltas.apurar(List.of(
                dia(SEGUNDA, h, "09:00", "12:00", "14:00", "17:00"),              // 6h: cumpre plataformas, fica 1h a dever
                dia(SEGUNDA.plusDays(1), h, "07:00", "12:00", "14:30", "18:00"),  // 8.5h: 30 min de plataforma em falta
                dia(SEGUNDA.plusDays(2), h, "08:00", "12:00", "13:00", "16:00"))); // 7h

        assertEquals(EstadoDiaApurado.SEM_FALTA, r.dias().get(0).estado());
        assertEquals(MotivoFalta.PLATAFORMA, r.dias().get(1).motivo());
        assertEquals(30, r.dias().get(1).minutosEmFalta());
        var debito = r.debitos().get(0);
        // Esperado 3x420 = 1260; trabalhado 360+510+420 = 1290: ha credito, nao ha debito.
        assertEquals(1260, debito.minutosEsperados());
        assertEquals(1290, debito.minutosTrabalhados());
        assertEquals(0, debito.minutosDebito());
        assertEquals(30, r.minutosParciais());
    }

    @Test
    void flexivelComDebitoNoFimDaSemana() {
        var h = flexivel();
        var r = ApuramentoFaltas.apurar(List.of(
                dia(SEGUNDA, h, "09:30", "12:00", "14:00", "16:30"),              // 5h, plataformas cumpridas
                dia(SEGUNDA.plusDays(1), h, "09:30", "12:00", "14:00", "16:30"))); // 5h
        assertEquals(240, r.debitos().get(0).minutosDebito());
        assertEquals(240, r.minutosParciais());
    }

    @Test
    void conversaoDoNumeroQuatro() {
        assertEquals(new BigDecimal("0.5"), ApuramentoFaltas.converter(5, 480));    // um atraso de 5 min no mes
        assertEquals(new BigDecimal("0.5"), ApuramentoFaltas.converter(240, 480));  // exactamente meio periodo
        assertEquals(new BigDecimal("1"), ApuramentoFaltas.converter(241, 480));    // acima de meio
        assertEquals(new BigDecimal("2"), ApuramentoFaltas.converter(960, 480));
        assertEquals(new BigDecimal("2.5"), ApuramentoFaltas.converter(1000, 480));
        assertEquals(BigDecimal.ZERO, ApuramentoFaltas.converter(0, 480));
    }

    private ApuramentoFaltas.Dia comJustificadas(ApuramentoFaltas.Dia d, String de, String ate) {
        return new ApuramentoFaltas.Dia(d.data(), d.estadoPrevio(), d.horario(), d.assiduidade(), d.temMarcacoesValidas(),
                List.of(new DiaAssiduidade.Periodo(LocalTime.parse(de), LocalTime.parse(ate))));
    }

    @Test
    void horaJustificadaCobreOAtrasoNoFixo() {
        // Entrou as 09:00; a hora das 08:00 as 09:00 esta justificada por um pedido em horas (V58).
        var r = ApuramentoFaltas.apurar(List.of(
                comJustificadas(dia(SEGUNDA, fixo(), "09:00", "12:00", "13:00", "17:00"), "08:00", "09:00")));
        var d = r.dias().get(0);
        assertEquals(EstadoDiaApurado.SEM_FALTA, d.estado());
        assertEquals(60, d.minutosJustificados());
        assertEquals(0, r.minutosParciais());
    }

    @Test
    void semMarcacoesMasComHorasJustificadasNaoEDiaInteiro() {
        var r = ApuramentoFaltas.apurar(List.of(comJustificadas(dia(SEGUNDA, fixo()), "08:00", "10:00")));
        var d = r.dias().get(0);
        assertEquals(MotivoFalta.INCOMPLETO, d.motivo());
        assertEquals(360, d.minutosEmFalta());
        assertEquals(0, r.diasSemRegisto());
    }

    @Test
    void horaPicadaEJustificadaNaoContaDuasVezes() {
        var r = ApuramentoFaltas.apurar(List.of(
                comJustificadas(dia(SEGUNDA, fixo(), "08:00", "12:00", "13:00", "17:00"), "11:00", "12:00")));
        assertEquals(0, r.dias().get(0).minutosJustificados());
    }

    @Test
    void noFlexivelAsHorasJustificadasContamNoDebito() {
        var h = flexivel();
        // 5h picadas + 2h de amamentacao justificadas = 7h: sem debito.
        var r = ApuramentoFaltas.apurar(List.of(
                comJustificadas(dia(SEGUNDA, h, "09:30", "12:00", "14:00", "16:30"), "16:30", "18:30")));
        assertEquals(0, r.debitos().get(0).minutosDebito());
        assertEquals(120, r.dias().get(0).minutosJustificados());
    }

    @Test
    void oParcialSomaSeNoMesAntesDeConverter() {
        var h = fixo();
        // Tres atrasos de 30 min em tres dias: 90 min no mes -> meia falta, nao tres meias.
        var r = ApuramentoFaltas.apurar(List.of(
                dia(SEGUNDA, h, "08:30", "12:00", "13:00", "17:00"),
                dia(SEGUNDA.plusDays(1), h, "08:30", "12:00", "13:00", "17:00"),
                dia(SEGUNDA.plusDays(2), h, "08:30", "12:00", "13:00", "17:00")));
        assertEquals(90, r.minutosParciais());
        assertEquals(480, r.periodoNormalMinutos());
        assertEquals(new BigDecimal("0.5"), r.faltasParciais());
    }
}
