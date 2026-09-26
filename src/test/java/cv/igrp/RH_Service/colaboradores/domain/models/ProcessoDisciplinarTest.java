package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/** BR-DIS: fases, prazos (arts. 48.º, 62.º, 71.º, 72.º, 84.º), suspensão preventiva, prescrição, execução e recurso. */
public class ProcessoDisciplinarTest {

    /** 2026-03-02 é segunda-feira. */
    public static final LocalDate D = LocalDate.of(2026, 3, 2);
    public static final Predicate<LocalDate> UTIL = d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY;
    private final FuncionarioId arguido = FuncionarioId.gerarNovo();
    private final FuncionarioId instrutor = FuncionarioId.gerarNovo();

    public static ProcessoDisciplinar instruido(FuncionarioId arguido, FuncionarioId instrutor, PenaDisciplinar prevista) {
        var p = ProcessoDisciplinar.participar(arguido, "PD/2026/001", null, D.minusDays(10), D, "Faltou ao respeito", prevista, D);
        p.instaurar("Despacho 1/2026", D, "Director");
        p.nomearInstrutor(instrutor, "Instrutora", D);
        p.iniciarInstrucao(D.plusDays(2));
        return p;
    }

    public static ProcessoDisciplinar decidido(FuncionarioId arguido, FuncionarioId instrutor, PenaDisciplinar pena, Integer duracao) {
        var p = instruido(arguido, instrutor, pena);
        p.acusar(D.plusDays(20), pena, "Violou o dever de correcção");
        p.notificarAcusacao(D.plusDays(21), 10, false);
        p.registarDefesa(D.plusDays(25), "Nega");
        p.relatorio(D.plusDays(30), pena, duracao, "Provado", D.plusDays(30));
        p.decidir(D.plusDays(35), pena, duracao, "Director", null, true);
        return p;
    }

    @Test
    void prazosDaInstrucaoEProrrogacaoUnica() {
        var p = ProcessoDisciplinar.participar(arguido, "PD/2026/001", null, D.minusDays(10), D, "Factos", PenaDisciplinar.MULTA, D);
        p.instaurar("Despacho", D, null);
        p.nomearInstrutor(instrutor, null, D);
        // 3 dias úteis a partir de segunda: quinta.
        assertEquals(D.plusDays(3), p.prazos(D, UTIL).get(0).data());
        p.iniciarInstrucao(D.plusDays(1));
        assertEquals(D.plusDays(31), p.fimDaInstrucao());
        p.prorrogarInstrucao(null, D.plusDays(20));
        assertEquals(D.plusDays(46), p.fimDaInstrucao());
        assertThrows(IgrpResponseStatusException.class, () -> p.prorrogarInstrucao(10, D.plusDays(21)));
    }

    @Test
    void arguidoNaoEInstrutorEInstrucaoPrecisaDeInstrutor() {
        var p = ProcessoDisciplinar.participar(arguido, "X", null, null, D, "Factos", null, D);
        p.instaurar("Despacho", D, null);
        assertThrows(IgrpResponseStatusException.class, () -> p.nomearInstrutor(arguido, null, D));
        assertThrows(IgrpResponseStatusException.class, () -> p.iniciarInstrucao(D));
    }

    @Test
    void suspensaoPreventivaSoComPenaGraveEAte90Dias() {
        var leve = instruido(arguido, instrutor, PenaDisciplinar.MULTA);
        assertThrows(IgrpResponseStatusException.class, () -> leve.suspenderPreventivamente(D.plusDays(3), 30, false, D.plusDays(3)));
        var p = instruido(arguido, instrutor, PenaDisciplinar.SUSPENSAO);
        p.suspenderPreventivamente(D.plusDays(3), 60, true, D.plusDays(3));
        assertThrows(IgrpResponseStatusException.class, () -> p.suspenderPreventivamente(D.plusDays(70), 31, false, D.plusDays(70)));
        p.levantarSuspensao(D.plusDays(10));
        assertEquals(D.plusDays(9), p.suspensoes().get(0).dataFim());
        // Levantada ao 8.º dia: ficam 82 dos 90.
        p.suspenderPreventivamente(D.plusDays(20), 82, false, D.plusDays(20));
    }

    @Test
    void defesaNoPrazoERelatorioSoDepois() {
        var p = instruido(arguido, instrutor, PenaDisciplinar.SUSPENSAO);
        p.acusar(D.plusDays(20), PenaDisciplinar.SUSPENSAO, "Acusação");
        assertThrows(IgrpResponseStatusException.class, () -> p.notificarAcusacao(D.plusDays(21), 25, false));
        p.notificarAcusacao(D.plusDays(21), 30, true);
        assertEquals(D.plusDays(51), p.ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_ACUSACAO).orElseThrow().dataFim());
        assertThrows(IgrpResponseStatusException.class, () -> p.relatorio(D.plusDays(40), PenaDisciplinar.SUSPENSAO, 30, "R", D.plusDays(40)));
        assertThrows(IgrpResponseStatusException.class, () -> p.registarDefesa(D.plusDays(52), "Tarde"));
        // Sem resposta: vale como audiência — o relatório já pode vir depois do prazo.
        p.relatorio(D.plusDays(52), PenaDisciplinar.SUSPENSAO, 30, "R", D.plusDays(52));
        assertEquals(FaseProcessoDisciplinar.RELATORIO, p.getFase());
    }

    @Test
    void decisaoDiferenteDoRelatorioEFundamentadaEDuracoesDaLei() {
        var p = instruido(arguido, instrutor, PenaDisciplinar.SUSPENSAO);
        p.acusar(D.plusDays(20), PenaDisciplinar.SUSPENSAO, "A");
        p.notificarAcusacao(D.plusDays(21), 10, false);
        p.registarDefesa(D.plusDays(22), "D");
        assertThrows(IgrpResponseStatusException.class, () -> p.relatorio(D.plusDays(30), PenaDisciplinar.SUSPENSAO, 10, "R", D.plusDays(30)));
        p.relatorio(D.plusDays(30), PenaDisciplinar.SUSPENSAO, 30, "R", D.plusDays(30));
        assertThrows(IgrpResponseStatusException.class, () -> p.decidir(D.plusDays(35), PenaDisciplinar.SUSPENSAO, 60, "D", null, false));
        p.decidir(D.plusDays(35), PenaDisciplinar.SUSPENSAO, 60, "D", "Reincidência", false);
        assertEquals("Suspensão (60 dias)", p.getPenalty());
    }

    @Test
    void prescricaoAntesDaInstauracaoNaoPune() {
        var p = ProcessoDisciplinar.participar(arguido, "X", null, D.minusMonths(8), D, "Factos", PenaDisciplinar.CENSURA_ESCRITA, D);
        assertEquals(D.minusMonths(2), p.prescreveEm());
        p.instaurar("Despacho", D, null);
        p.nomearInstrutor(instrutor, null, D);
        p.iniciarInstrucao(D);
        p.acusar(D.plusDays(5), PenaDisciplinar.CENSURA_ESCRITA, "A");
        p.notificarAcusacao(D.plusDays(5), 10, false);
        p.registarDefesa(D.plusDays(6), "D");
        p.relatorio(D.plusDays(7), PenaDisciplinar.CENSURA_ESCRITA, null, "R", D.plusDays(7));
        assertThrows(IgrpResponseStatusException.class, () -> p.decidir(D.plusDays(8), PenaDisciplinar.CENSURA_ESCRITA, null, "D", null, false));
    }

    @Test
    void penaLeveExecutaNoDiaSeguinteEExpulsivaDepoisDoRecurso() {
        var leve = decidido(arguido, instrutor, PenaDisciplinar.SUSPENSAO, 30);
        leve.notificarDecisao(D.plusDays(40));
        assertEquals(D.plusDays(41), leve.dataExecucao());
        assertEquals(D.plusDays(41), leve.getPenaltyStartDate());
        assertEquals(D.plusDays(70), leve.getPenaltyEndDate());
        var grave = decidido(arguido, instrutor, PenaDisciplinar.DEMISSAO, null);
        grave.notificarDecisao(D.plusDays(40));
        assertEquals(D.plusDays(56), grave.dataExecucao());
        assertFalse(grave.efeitosDevidos(D.plusDays(55)));
    }

    @Test
    void recursoNoPrazoSuspendeEDiminuiOuAnula() {
        var p = decidido(arguido, instrutor, PenaDisciplinar.DEMISSAO, null);
        p.notificarDecisao(D.plusDays(40));
        assertThrows(IgrpResponseStatusException.class, () -> p.interporRecurso(D.plusDays(56), null));
        p.interporRecurso(D.plusDays(50), "Recorre");
        assertNull(p.dataExecucao());
        assertThrows(IgrpResponseStatusException.class,
                () -> p.decidirRecurso(D.plusDays(60), ProcessoDisciplinar.ResultadoRecurso.DIMINUIDA, PenaDisciplinar.DEMISSAO, null));
        p.decidirRecurso(D.plusDays(60), ProcessoDisciplinar.ResultadoRecurso.DIMINUIDA, PenaDisciplinar.SUSPENSAO, 90);
        assertEquals(D.plusDays(61), p.dataExecucao());
        assertEquals(D.plusDays(61), p.getPenaltyStartDate());

        var q = decidido(arguido, instrutor, PenaDisciplinar.MULTA, 5);
        q.notificarDecisao(D.plusDays(40));
        q.marcarEfeitosAplicados(D.plusDays(41), LocalDateTime.now(), "Executada");
        assertEquals(FaseProcessoDisciplinar.NOTIFICADO, q.getFase());
        q.interporRecurso(D.plusDays(45), null);
        q.decidirRecurso(D.plusDays(50), ProcessoDisciplinar.ResultadoRecurso.ANULADA, null, null);
        assertEquals(FaseProcessoDisciplinar.CONCLUIDO, q.getFase());
        assertNull(q.getPena());
    }

    @Test
    void concluiQuandoTransitaEArquivaSemPena() {
        var p = decidido(arguido, instrutor, PenaDisciplinar.CENSURA_ESCRITA, null);
        p.notificarDecisao(D.plusDays(40));
        p.marcarEfeitosAplicados(D.plusDays(41), LocalDateTime.now(), "Executada");
        assertFalse(p.concluirSeTransitado(D.plusDays(55)));
        assertTrue(p.concluirSeTransitado(D.plusDays(56)));

        var a = instruido(arguido, instrutor, null);
        a.relatorio(D.plusDays(20), null, null, "Não há infracção", D.plusDays(20));
        a.decidir(D.plusDays(25), null, null, "Director", null, false);
        assertEquals(FaseProcessoDisciplinar.ARQUIVADO, a.getFase());
        assertFalse(a.arguidoEmCurso());
    }

    @Test
    void registoAntigoNaoTemTramitacaoETramitadoNaoMudaAPenaAMao() {
        var antigo = ProcessoDisciplinar.criar(arguido, "P-1", D, null, "Multa", null, null, null, null);
        assertThrows(IgrpResponseStatusException.class, () -> antigo.instaurar("D", D, null));
        antigo.atualizar(null, null, null, "Censura", null, null, null, null);
        assertEquals("Censura", antigo.getPenalty());
        var novo = decidido(arguido, instrutor, PenaDisciplinar.MULTA, 5);
        assertThrows(IgrpResponseStatusException.class, () -> novo.atualizar(null, null, null, "Outra", null, null, null, null));
        novo.atualizar(null, null, null, null, null, null, "BO n.º 12", "nota");
    }
}
