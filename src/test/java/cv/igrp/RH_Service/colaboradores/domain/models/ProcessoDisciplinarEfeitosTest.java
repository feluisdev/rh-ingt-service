package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** BR-DIS-26..28: suspensão da pena (art. 34.º), reabilitação (art. 95.º), revisão (arts. 90.º–94.º). */
class ProcessoDisciplinarEfeitosTest {

    private static final LocalDate D = ProcessoDisciplinarTest.D;
    private final FuncionarioId arguido = FuncionarioId.gerarNovo();
    private final FuncionarioId instrutor = FuncionarioId.gerarNovo();

    private ProcessoDisciplinar comRelatorio(PenaDisciplinar pena, Integer duracao) {
        var p = ProcessoDisciplinarTest.instruido(arguido, instrutor, pena);
        p.acusar(D.plusDays(20), pena, "A");
        p.notificarAcusacao(D.plusDays(21), 10, false);
        p.registarDefesa(D.plusDays(22), "D");
        p.relatorio(D.plusDays(30), pena, duracao, "R", D.plusDays(30));
        return p;
    }

    @Test
    void penaSuspensaNaoExecutaECaducaSePunidoDeNovo() {
        var p = comRelatorio(PenaDisciplinar.SUSPENSAO, 30);
        assertThrows(IgrpResponseStatusException.class, () -> p.decidir(D.plusDays(35), PenaDisciplinar.SUSPENSAO, 30, "D", null, false, 4));
        p.decidir(D.plusDays(35), PenaDisciplinar.SUSPENSAO, 30, "D", null, false, 2);
        p.notificarDecisao(D.plusDays(40));
        assertNull(p.dataExecucao());
        assertNull(p.getPenaltyStartDate());
        assertEquals(D.plusDays(40).plusYears(2), p.suspensaAte());
        assertTrue(p.concluirSeTransitado(D.plusDays(56)));
        assertTrue(p.penaSuspensaEm(D.plusDays(300)));
        p.caducarSuspensao(D.plusDays(300), "Punido de novo");
        assertEquals(D.plusDays(301), p.dataExecucao());
        assertEquals(D.plusDays(301), p.getPenaltyStartDate());
        assertTrue(p.efeitosDevidos(D.plusDays(301)));
        assertThrows(IgrpResponseStatusException.class, () -> p.caducarSuspensao(D.plusDays(302), "outra"));
    }

    @Test
    void soAMultaEASuspensaoSeSuspendem() {
        var p = comRelatorio(PenaDisciplinar.INACTIVIDADE, 6);
        assertThrows(IgrpResponseStatusException.class, () -> p.decidir(D.plusDays(35), PenaDisciplinar.INACTIVIDADE, 6, "D", null, false, 1));
    }

    @Test
    void reabilitacaoSoDasExpulsivasPassados5Anos() {
        var p = ProcessoDisciplinarTest.decidido(arguido, instrutor, PenaDisciplinar.DEMISSAO, null);
        p.notificarDecisao(D.plusDays(40));
        LocalDate execucao = D.plusDays(56);
        p.marcarEfeitosAplicados(execucao, LocalDateTime.now(), "Executada");
        assertThrows(IgrpResponseStatusException.class, () -> p.reabilitar(execucao.plusYears(5).minusDays(1), "Despacho"));
        p.reabilitar(execucao.plusYears(5), "Despacho 3/2031");
        assertThrows(IgrpResponseStatusException.class, () -> p.reabilitar(execucao.plusYears(6), "outra"));
        var multa = ProcessoDisciplinarTest.decidido(arguido, instrutor, PenaDisciplinar.MULTA, 5);
        assertThrows(IgrpResponseStatusException.class, () -> multa.reabilitar(D.plusYears(10), "D"));
    }

    @Test
    void revisaoNaoAgravaERevogaApagaAPena() {
        var p = ProcessoDisciplinarTest.decidido(arguido, instrutor, PenaDisciplinar.SUSPENSAO, 60);
        p.notificarDecisao(D.plusDays(40));
        p.marcarEfeitosAplicados(D.plusDays(41), LocalDateTime.now(), "Executada");
        assertThrows(IgrpResponseStatusException.class,
                () -> p.rever(D.plusDays(100), ProcessoDisciplinar.ResultadoRevisao.ALTERADA, PenaDisciplinar.SUSPENSAO, 90, "D"));
        assertThrows(IgrpResponseStatusException.class,
                () -> p.rever(D.plusDays(100), ProcessoDisciplinar.ResultadoRevisao.ALTERADA, PenaDisciplinar.INACTIVIDADE, 6, "D"));
        p.rever(D.plusDays(100), ProcessoDisciplinar.ResultadoRevisao.ALTERADA, PenaDisciplinar.SUSPENSAO, 30, "Despacho");
        assertEquals(D.plusDays(70), p.getPenaltyEndDate());
        p.rever(D.plusDays(200), ProcessoDisciplinar.ResultadoRevisao.REVOGADA, null, null, "Despacho 2");
        assertNull(p.getPena());
        assertEquals("Revogada em revisão", p.getPenalty());
        assertFalse(p.periodosDeAfastamento().stream().anyMatch(x -> x[0] != null && x[0].equals(D.plusDays(41))));
    }
}
