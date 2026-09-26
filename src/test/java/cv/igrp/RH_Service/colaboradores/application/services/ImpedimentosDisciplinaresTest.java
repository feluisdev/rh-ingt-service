package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.PenaDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinarTest;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/** BR-DIS-29: concurso durante a suspensão; promoção durante a pena e no ano a seguir à inactividade; 2 anos sem ser dirigente. */
class ImpedimentosDisciplinaresTest {

    private static final LocalDate D = ProcessoDisciplinarTest.D;
    private final ProcessoDisciplinarRepository repository = mock(ProcessoDisciplinarRepository.class);
    private final ImpedimentosDisciplinares impedimentos = new ImpedimentosDisciplinares(repository);
    private final FuncionarioId arguido = FuncionarioId.gerarNovo();

    private ProcessoDisciplinar executado(PenaDisciplinar pena, Integer duracao, String efeitos) {
        var p = ProcessoDisciplinarTest.decidido(arguido, FuncionarioId.gerarNovo(), pena, duracao);
        p.notificarDecisao(D.plusDays(40));
        p.marcarEfeitosAplicados(D.plusDays(41), LocalDateTime.now(), efeitos);
        when(repository.findComAfastamento(arguido)).thenReturn(List.of(p));
        when(repository.findAllByFuncionarioId(arguido)).thenReturn(List.of(p));
        return p;
    }

    @Test
    void suspensaoImpedeConcursoEPromocaoSoDuranteAPena() {
        executado(PenaDisciplinar.SUSPENSAO, 30, "Executada.");
        assertTrue(impedimentos.impedeConcurso(arguido, D.plusDays(50)).isPresent());
        assertTrue(impedimentos.impedePromocao(arguido, D.plusDays(50)).isPresent());
        assertFalse(impedimentos.impedeConcurso(arguido, D.plusDays(71)).isPresent());
        assertFalse(impedimentos.impedePromocao(arguido, D.plusDays(71)).isPresent());
    }

    @Test
    void inactividadeImpedePromocaoAteUmAnoDepoisMasNaoOConcurso() {
        var p = executado(PenaDisciplinar.INACTIVIDADE, 6, "Executada.");
        assertFalse(impedimentos.impedeConcurso(arguido, D.plusDays(50)).isPresent());
        assertTrue(impedimentos.impedePromocao(arguido, p.getPenaltyEndDate().plusMonths(11)).isPresent());
        assertFalse(impedimentos.impedePromocao(arguido, p.getPenaltyEndDate().plusYears(1).plusDays(1)).isPresent());
    }

    @Test
    void comissaoCessadaPorPenaImpedeNomeacaoDoisAnos() {
        executado(PenaDisciplinar.MULTA, 5, "Facto da pena no diário; Comissão de serviço cessada.");
        assertTrue(impedimentos.impedeNomeacaoDirigente(arguido, D.plusDays(40).plusYears(2).minusDays(1)).isPresent());
        assertFalse(impedimentos.impedeNomeacaoDirigente(arguido, D.plusDays(40).plusYears(2)).isPresent());
    }
}
