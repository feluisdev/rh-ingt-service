package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-APO-04: avisos aos 180, 90 e 30 dias e no dia; o colaborador so aos 180 e no dia. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlertaAposentacaoJobTest {

    private static final LocalDate DIA = LocalDate.of(2026, 9, 25);
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private AposentacaoService aposentacaoService;
    @Mock private NotificacaoRepository notificacaoRepository;
    private AlertaAposentacaoJob job;
    private Funcionario pessoa;

    @BeforeEach
    void setUp() {
        job = new AlertaAposentacaoJob(funcionarioRepository, aposentacaoService, new Notificador(notificacaoRepository));
        pessoa = mock(Funcionario.class);
        when(pessoa.getId()).thenReturn(FuncionarioId.gerarNovo());
        when(pessoa.getNomeCompleto()).thenReturn("Ana");
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void aos180DiasAvisaORhEOColaborador() {
        when(aposentacaoService.limiteEfectivo(pessoa)).thenReturn(DIA.plusDays(180));
        assertTrue(job.avisar(pessoa, DIA));
        verify(notificacaoRepository, times(2)).save(any());
    }

    @Test
    void aos90DiasSoORh() {
        when(aposentacaoService.limiteEfectivo(pessoa)).thenReturn(DIA.plusDays(90));
        assertTrue(job.avisar(pessoa, DIA));
        verify(notificacaoRepository, times(1)).save(any());
    }

    @Test
    void foraDosMarcosNaoAvisa() {
        when(aposentacaoService.limiteEfectivo(pessoa)).thenReturn(DIA.plusDays(91));
        assertFalse(job.avisar(pessoa, DIA));
        when(aposentacaoService.limiteEfectivo(pessoa)).thenReturn(null);
        assertFalse(job.avisar(pessoa, DIA));
        verify(notificacaoRepository, never()).save(any());
    }
}
