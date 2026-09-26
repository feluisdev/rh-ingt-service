package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Exoneracao;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.ExoneracaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-EXO: condicionantes (arguido, garantia), efeito imediato quando o dia passou, um pedido de cada vez. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExoneracaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 1);
    @Mock private ExoneracaoRepository repository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ProcessoDisciplinarRepository processoRepository;
    @Mock private GarantiasDeFormacao garantias;
    @Mock private CessacaoService cessacaoService;
    @Mock private ChefiaService chefiaService;
    @Mock private NotificacaoRepository notificacaoRepository;
    private ExoneracaoService service;
    private final FuncionarioId f = FuncionarioId.gerarNovo();

    @BeforeEach
    void setUp() {
        service = new ExoneracaoService(repository, funcionarioRepository, processoRepository, List.of(garantias), cessacaoService, chefiaService,
                new Notificador(notificacaoRepository)) {
            @Override LocalDate hoje() { return HOJE; }
        };
        var func = mock(Funcionario.class);
        when(func.getIsActive()).thenReturn(true);
        when(func.getNomeCompleto()).thenReturn("Rui");
        when(funcionarioRepository.findById(any())).thenReturn(Optional.of(func));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(repository.findByFuncionario(f)).thenReturn(List.of());
        when(garantias.emCurso(any(), any())).thenReturn(List.of());
        when(processoRepository.findAllByFuncionarioId(any())).thenReturn(List.of());
        when(cessacaoService.estadoDeCessacaoPorOmissao()).thenReturn(mock(WorkerState.class));
    }

    @Test
    void condicionantesDoArguidoEDaGarantia() {
        when(processoRepository.existeArguidoEmCurso(f)).thenReturn(true);
        when(garantias.emCurso(eq(f), any())).thenReturn(List.of("Prazo de garantia da formação «X» até 01/01/2027 (art. 95.º b))."));
        var r = service.pedir(f, HOJE, null, null, true);
        assertEquals(2, r.condicionantes().size());
    }

    @Test
    void deferidaComODiaJaPassadoProduzEfeitosLogo() {
        var e = Exoneracao.pedir(f, HOJE.minusDays(70), null, null, true, HOJE.minusDays(70));
        when(repository.findById(e.getId())).thenReturn(Optional.of(e));
        service.deferir(f, e.getId(), "Despacho 1/2026", HOJE);
        assertEquals(Exoneracao.Estado.EFECTIVADA, e.getEstado());
        verify(cessacaoService).cessar(eq(f), any(), eq(HOJE.minusDays(10)), eq(ExoneracaoService.MOTIVO_CESSACAO), any());
    }

    @Test
    void condicionadaEsperaEUmPedidoDeCadaVez() {
        when(processoRepository.existeArguidoEmCurso(f)).thenReturn(true);
        var e = Exoneracao.pedir(f, HOJE.minusDays(70), null, null, true, HOJE.minusDays(70));
        when(repository.findById(e.getId())).thenReturn(Optional.of(e));
        service.deferir(f, e.getId(), "Despacho 1/2026", HOJE);
        assertEquals(Exoneracao.Estado.DEFERIDA, e.getEstado());
        verify(cessacaoService, never()).cessar(any(), any(), any(), any(), any());
        when(repository.findByFuncionario(f)).thenReturn(List.of(e));
        assertThrows(IgrpResponseStatusException.class, () -> service.pedir(f, HOJE, null, null, true));
    }
}
