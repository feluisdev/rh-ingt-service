package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ContagemDias;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.PenaDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinarTest;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/** BR-DIS-30: sem férias durante a pena de suspensão/inactividade nem no ano seguinte; 10 dias se a suspensão foi de 90 dias ou menos. */
class BloqueioFeriasDisciplinarTest {

    private static final LocalDate D = ProcessoDisciplinarTest.D;
    private final ProcessoDisciplinarRepository processos = mock(ProcessoDisciplinarRepository.class);
    private final PedidoAusenciaRepository pedidos = mock(PedidoAusenciaRepository.class);
    private final TipoAusenciaRepository tipos = mock(TipoAusenciaRepository.class);
    private final ImpedimentosDisciplinares impedimentos = new ImpedimentosDisciplinares(processos);
    private final BloqueioFeriasDisciplinar bloqueio = new BloqueioFeriasDisciplinar(impedimentos, pedidos, tipos);
    private final FuncionarioId arguido = FuncionarioId.gerarNovo();
    private final TipoAusencia ferias = tipo(RegimeAusencia.FERIAS);
    private final TipoAusencia falta = tipo(RegimeAusencia.FALTA);

    BloqueioFeriasDisciplinarTest() {
        when(tipos.findById(ferias.getId())).thenReturn(java.util.Optional.of(ferias));
        when(tipos.findById(falta.getId())).thenReturn(java.util.Optional.of(falta));
        when(pedidos.findAprovadosEntre(any(), any(), any())).thenReturn(List.of());
        when(pedidos.findPendentesDe(any())).thenReturn(List.of());
    }

    private static TipoAusencia tipo(RegimeAusencia regime) {
        return TipoAusencia.reconstituir(TipoAusenciaId.gerarNovo(), regime.name(), regime.name(), true, true, null, null, null,
                "GOZAMENTO", true, regime, null, ContagemDias.DIAS_UTEIS);
    }

    private ProcessoDisciplinar executado(PenaDisciplinar pena, int duracao) {
        var p = ProcessoDisciplinarTest.decidido(arguido, FuncionarioId.gerarNovo(), pena, duracao);
        p.notificarDecisao(D.plusDays(40));
        p.marcarEfeitosAplicados(D.plusDays(41), LocalDateTime.now(), "Executada.");
        when(processos.findComAfastamento(arguido)).thenReturn(List.of(p));
        return p;
    }

    private PedidoAusencia pedido(TipoAusencia tipo, LocalDate de, int dias) {
        return PedidoAusencia.criar(arguido, tipo.getId(), de, de.plusDays(dias - 1), dias, null, null);
    }

    @Test
    void suspensaoCurtaDeixaDezDiasNoAnoSeguinte() {
        var p = executado(PenaDisciplinar.SUSPENSAO, 30);
        var janela = impedimentos.janelasSemFerias(arguido).getFirst();
        assertEquals(10, janela.diasPermitidos());
        assertEquals(p.getPenaltyEndDate().plusYears(1), janela.ate());

        var depois = p.getPenaltyEndDate().plusDays(20);
        assertTrue(bloqueio.motivo(pedido(ferias, depois, 10)).isEmpty());
        var excesso = assertThrows(IgrpResponseStatusException.class, () -> bloqueio.verificar(pedido(ferias, depois, 11)));
        assertTrue(excesso.getMessage().contains("só pode gozar 10 dias"));

        // os dias já marcados contam para os 10
        when(pedidos.findPendentesDe(any())).thenReturn(List.of(pedido(ferias, depois.plusMonths(2), 6)));
        assertTrue(bloqueio.motivo(pedido(ferias, depois, 5)).isPresent());
        assertTrue(bloqueio.motivo(pedido(ferias, depois, 4)).isEmpty());
    }

    @Test
    void duranteAPenaNaoHaFerias() {
        var p = executado(PenaDisciplinar.SUSPENSAO, 30);
        var m = bloqueio.motivo(pedido(ferias, p.getPenaltyStartDate().plusDays(3), 2));
        assertTrue(m.isPresent());
        assertTrue(m.get().contains("está a cumprir pena de suspensão"));
    }

    @Test
    void suspensaoLongaEInactividadeNaoDeixamDias() {
        var p = executado(PenaDisciplinar.SUSPENSAO, 100);
        assertEquals(0, impedimentos.janelasSemFerias(arguido).getFirst().diasPermitidos());
        assertTrue(bloqueio.motivo(pedido(ferias, p.getPenaltyEndDate().plusDays(10), 1)).isPresent());
        assertTrue(bloqueio.motivo(pedido(ferias, p.getPenaltyEndDate().plusYears(1).plusDays(1), 22)).isEmpty());

        var i = executado(PenaDisciplinar.INACTIVIDADE, 6);
        assertEquals(0, impedimentos.janelasSemFerias(arguido).getFirst().diasPermitidos());
        assertTrue(bloqueio.motivo(pedido(ferias, i.getPenaltyEndDate().plusMonths(11), 1)).isPresent());
    }

    @Test
    void soAsFeriasEAsPenasDeAfastamento() {
        var p = executado(PenaDisciplinar.SUSPENSAO, 100);
        assertTrue(bloqueio.motivo(pedido(falta, p.getPenaltyEndDate().plusDays(10), 3)).isEmpty());
        executado(PenaDisciplinar.MULTA, 5);
        assertFalse(impedimentos.janelasSemFerias(arguido).stream().findAny().isPresent());
    }
}
