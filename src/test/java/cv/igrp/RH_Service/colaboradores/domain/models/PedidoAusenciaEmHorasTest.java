package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

/** O pedido em horas (V58): duracao por dia, e terminar antes do fim (a decisao fica; acaba na vespera). */
class PedidoAusenciaEmHorasTest {

    private static final LocalDate INICIO = LocalDate.of(2027, 3, 1);
    private static final LocalDate FIM = LocalDate.of(2027, 8, 31);

    private static PedidoAusencia amamentacao() {
        var p = PedidoAusencia.criar(FuncionarioId.gerarNovo(), TipoAusenciaId.gerarNovo(), INICIO, FIM, 0, "amamentacao", null);
        p.definirHoras(LocalTime.of(8, 0), LocalTime.of(9, 0));
        return p;
    }

    @Test
    void semHorasEDeDiasInteiros() {
        var p = PedidoAusencia.criar(FuncionarioId.gerarNovo(), TipoAusenciaId.gerarNovo(), INICIO, INICIO, 1, "x", null);
        p.definirHoras(null, null);
        assertFalse(p.isEmHoras());
        assertEquals(0, p.minutosPorDia());
        assertEquals(INICIO, p.ultimoDiaEmVigor());
    }

    @Test
    void minutosPorDia() {
        assertEquals(60, amamentacao().minutosPorDia());
    }

    @Test
    void terminarAntesDoFimAcabaNaVespera() {
        var p = amamentacao();
        p.aprovar(FuncionarioId.gerarNovo(), INICIO, "ok");
        p.terminar(LocalDate.of(2027, 6, 1), "deixou de amamentar");
        assertEquals(LocalDate.of(2027, 5, 31), p.ultimoDiaEmVigor());
        assertEquals(EstadoPedidoAusencia.APROVADO, p.getEstado());
    }

    @Test
    void terminarSoAprovadoDentroDoPeriodoEUmaVez() {
        var pendente = amamentacao();
        assertEquals(409, assertThrows(IgrpResponseStatusException.class,
                () -> pendente.terminar(LocalDate.of(2027, 6, 1), "x")).getStatusCode().value());

        var p = amamentacao();
        p.aprovar(FuncionarioId.gerarNovo(), INICIO, "ok");
        assertEquals(422, assertThrows(IgrpResponseStatusException.class, () -> p.terminar(INICIO, "x")).getStatusCode().value());
        assertEquals(422, assertThrows(IgrpResponseStatusException.class, () -> p.terminar(FIM.plusDays(1), "x")).getStatusCode().value());
        assertEquals(422, assertThrows(IgrpResponseStatusException.class, () -> p.terminar(LocalDate.of(2027, 6, 1), " ")).getStatusCode().value());
        p.terminar(LocalDate.of(2027, 6, 1), "fim");
        assertEquals(409, assertThrows(IgrpResponseStatusException.class,
                () -> p.terminar(LocalDate.of(2027, 7, 1), "outra")).getStatusCode().value());
    }

    @Test
    void naoSeTerminaUmPedidoDeDiasInteiros() {
        var p = PedidoAusencia.criar(FuncionarioId.gerarNovo(), TipoAusenciaId.gerarNovo(), INICIO, FIM, 100, "x", null);
        p.aprovar(FuncionarioId.gerarNovo(), INICIO, "ok");
        assertEquals(422, assertThrows(IgrpResponseStatusException.class,
                () -> p.terminar(LocalDate.of(2027, 6, 1), "x")).getStatusCode().value());
    }

    @Test
    void horasAoContrarioOuSoUma() {
        var p = PedidoAusencia.criar(FuncionarioId.gerarNovo(), TipoAusenciaId.gerarNovo(), INICIO, FIM, 0, "x", null);
        assertThrows(IgrpResponseStatusException.class, () -> p.definirHoras(LocalTime.of(9, 0), LocalTime.of(8, 0)));
        assertThrows(IgrpResponseStatusException.class, () -> p.definirHoras(LocalTime.of(9, 0), null));
    }
}
