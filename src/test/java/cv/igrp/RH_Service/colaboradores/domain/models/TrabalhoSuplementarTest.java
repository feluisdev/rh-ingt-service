package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Lei n.o 20/X/2023, art. 155.o n.o 2 a): a autorizacao, o ciclo de vida e as horas realizadas. */
class TrabalhoSuplementarTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 23, 10, 0);
    private static final LocalDate HOJE = AGORA.toLocalDate();
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();
    private final FuncionarioId chefe = FuncionarioId.gerarNovo();

    private static LocalTime h(String hhmm) { return LocalTime.parse(hhmm); }

    private static int status(Runnable r) {
        return assertThrows(IgrpResponseStatusException.class, r::run).getStatusCode().value();
    }

    @Test
    void lancadoPelaChefiaNasceAutorizado() {
        var t = TrabalhoSuplementar.lancar(pessoa, HOJE.plusDays(1), h("17:00"), h("19:30"), "Fecho de contas", chefe, AGORA);
        assertEquals(EstadoTrabalhoSuplementar.AUTORIZADO, t.getEstado());
        assertEquals(chefe, t.getDecididoPor());
        assertFalse(t.isAutorizacaoPosterior());
        assertFalse(t.isPedidoPeloProprio());
        assertEquals(150, t.minutosAutorizados());
    }

    @Test
    void lancadoParaUmDiaPassadoFicaAssinaladoComoAutorizacaoPosterior() {
        var t = TrabalhoSuplementar.lancar(pessoa, HOJE.minusDays(2), h("18:00"), h("20:00"), "Avaria urgente", null, AGORA);
        assertTrue(t.isAutorizacaoPosterior());
        assertNull(t.getDecididoPor());   // o RH
    }

    @Test
    void oProprioSoPedeParaHojeOuParaAFrente() {
        var t = TrabalhoSuplementar.pedir(pessoa, HOJE, h("17:00"), h("18:00"), "Entrega", AGORA);
        assertEquals(EstadoTrabalhoSuplementar.PEDIDO, t.getEstado());
        assertTrue(t.isPedidoPeloProprio());
        assertEquals(422, status(() -> TrabalhoSuplementar.pedir(pessoa, HOJE.minusDays(1), h("17:00"), h("18:00"), "x", AGORA)));
    }

    @Test
    void intervaloEMotivoSaoObrigatorios() {
        assertEquals(422, status(() -> TrabalhoSuplementar.lancar(pessoa, HOJE, h("18:00"), h("17:00"), "x", null, AGORA)));
        assertEquals(422, status(() -> TrabalhoSuplementar.lancar(pessoa, HOJE, h("18:00"), h("18:00"), "x", null, AGORA)));
        assertEquals(422, status(() -> TrabalhoSuplementar.lancar(pessoa, HOJE, null, h("18:00"), "x", null, AGORA)));
        assertEquals(422, status(() -> TrabalhoSuplementar.lancar(pessoa, HOJE, h("17:00"), h("18:00"), " ", null, AGORA)));
        assertEquals(422, status(() -> TrabalhoSuplementar.lancar(pessoa, null, h("17:00"), h("18:00"), "x", null, AGORA)));
    }

    @Test
    void soSeDecideUmPedidoERecusarExigeMotivo() {
        var t = TrabalhoSuplementar.pedir(pessoa, HOJE.plusDays(1), h("17:00"), h("18:00"), "Entrega", AGORA);
        assertEquals(422, status(() -> t.recusar(chefe, "", AGORA)));
        t.recusar(chefe, "Sem necessidade", AGORA);
        assertEquals(EstadoTrabalhoSuplementar.RECUSADO, t.getEstado());
        assertEquals("Sem necessidade", t.getMotivoRecusa());
        assertEquals(409, status(() -> t.autorizar(chefe, AGORA)));
    }

    @Test
    void autorizarUmPedidoDeUmDiaQueJaPassouEPosterior() {
        var t = TrabalhoSuplementar.pedir(pessoa, HOJE, h("17:00"), h("18:00"), "Entrega", AGORA);
        t.autorizar(null, AGORA.plusDays(1));
        assertTrue(t.isAutorizacaoPosterior());
    }

    @Test
    void cancelarPedidoOuAutorizadoComMotivo() {
        var t = TrabalhoSuplementar.lancar(pessoa, HOJE, h("17:00"), h("18:00"), "x", null, AGORA);
        assertEquals(422, status(() -> t.cancelar(null, AGORA)));
        t.cancelar("Afinal nao", AGORA);
        assertEquals(EstadoTrabalhoSuplementar.CANCELADO, t.getEstado());
        assertFalse(t.emVigor());
        assertEquals(409, status(() -> t.cancelar("de novo", AGORA)));
    }

    @Test
    void sobreposicaoETocarNoHorario() {
        var a = TrabalhoSuplementar.lancar(pessoa, HOJE, h("17:00"), h("19:00"), "x", null, AGORA);
        var b = TrabalhoSuplementar.lancar(pessoa, HOJE, h("18:30"), h("20:00"), "x", null, AGORA);
        var c = TrabalhoSuplementar.lancar(pessoa, HOJE, h("19:00"), h("20:00"), "x", null, AGORA);
        assertTrue(a.sobrepoe(b));
        assertFalse(a.sobrepoe(c));   // encostados nao se sobrepoem

        var bloco = List.of(new BlocoHorario(DayOfWeek.WEDNESDAY, h("13:00"), h("17:00"), true));
        assertFalse(a.tocaEm(bloco));
        assertTrue(TrabalhoSuplementar.lancar(pessoa, HOJE, h("16:30"), h("18:00"), "x", null, AGORA).tocaEm(bloco));
    }

    @Test
    void realizadasSaoAPresencaDentroDoIntervaloForaDosBlocos() {
        var t = TrabalhoSuplementar.lancar(pessoa, HOJE, h("17:00"), h("20:00"), "x", null, AGORA);
        // Entrou as 13:00 e saiu as 19:15: das 17:00 as 19:15 e suplementar.
        var presenca = List.of(new DiaAssiduidade.Periodo(h("13:00"), h("19:15")));
        assertEquals(135, t.minutosRealizados(presenca, List.of()));
        // Se o horario tivesse passado a ir ate as 17:30, essa meia hora e tempo normal.
        var blocos = List.of(new BlocoHorario(DayOfWeek.WEDNESDAY, h("13:30"), h("17:30"), true));
        assertEquals(105, t.minutosRealizados(presenca, blocos));
        // Sem presenca no intervalo, nada foi realizado.
        assertEquals(0, t.minutosRealizados(List.of(new DiaAssiduidade.Periodo(h("08:00"), h("12:00"))), List.of()));
    }
}
