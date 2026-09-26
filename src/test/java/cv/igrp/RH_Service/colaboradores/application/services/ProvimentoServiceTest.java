package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeProvimento;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoProva;
import cv.igrp.RH_Service.colaboradores.domain.models.Provimento;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProvimentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-PRV: com sucesso converte (e regista o facto); sem sucesso cessa; quem vem de outra carreira regressa. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProvimentoServiceTest {

    private static final LocalDate POSSE = LocalDate.of(2025, 9, 1);
    @Mock private ProvimentoRepository repository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private CessacaoService cessacaoService;
    @Mock private WorkerStateRepository workerStateRepository;
    @Mock private DiarioFactos diarioFactos;
    @Mock private NotificacaoRepository notificacaoRepository;
    @Mock private ChecklistService checklists;
    private ProvimentoService service;
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();
    private final FuncionarioId tutor = FuncionarioId.gerarNovo();

    @BeforeEach
    void setUp() {
        service = new ProvimentoService(repository, funcionarioRepository, contratoRepository, cessacaoService, workerStateRepository,
                diarioFactos, new Notificador(notificacaoRepository), checklists) {
            @Override LocalDate hoje() { return LocalDate.of(2026, 9, 5); }
        };
        var f = mock(Funcionario.class);
        when(f.getIsActive()).thenReturn(true);
        when(f.getNomeCompleto()).thenReturn("Ana");
        when(funcionarioRepository.findById(any())).thenReturn(Optional.of(f));
        when(repository.save(any(Provimento.class))).thenAnswer(i -> i.getArgument(0));
        when(repository.save(any(PeriodoProva.class))).thenAnswer(i -> i.getArgument(0));
        when(cessacaoService.estadoDeCessacaoPorOmissao()).thenReturn(mock(WorkerState.class));
    }

    private PeriodoProva estagio(boolean vemDeOutraCarreira) {
        var p = Provimento.registar(pessoa, ModalidadeProvimento.NOMEACAO_PROVISORIA, "D-1", POSSE, POSSE, "C-12", vemDeOutraCarreira, null, null);
        var e = PeriodoProva.estagio(p.getId(), pessoa, POSSE, tutor);
        p.comPeriodoProva(e.getId());
        when(repository.findById(p.getId())).thenReturn(Optional.of(p));
        when(repository.findPeriodo(e.getId())).thenReturn(Optional.of(e));
        return e;
    }

    @Test
    void estagioComSucessoPassaANomeacaoDefinitivaERegistaOFacto() {
        var e = estagio(false);
        var r = service.concluir(pessoa, e.getId(), PeriodoProva.Avaliacao.POSITIVA, "Revelou as competencias", LocalDate.of(2026, 9, 1), null);
        var c = ArgumentCaptor.forClass(Provimento.class);
        verify(repository, org.mockito.Mockito.atLeastOnce()).save(c.capture());
        var novo = c.getAllValues().get(c.getAllValues().size() - 1);
        assertEquals(ModalidadeProvimento.NOMEACAO_DEFINITIVA, novo.getModalidade());
        assertEquals(LocalDate.of(2026, 9, 1), novo.getDataPosse());
        verify(diarioFactos).registar(eq(pessoa), eq(TipoFactoRh.PROVIMENTO), any(), anyString(), any(), anyString(), any());
        verify(cessacaoService, never()).cessar(any(), any(), any(), any(), any());
        assertTrue(r.alertas().get(0).contains("tempo do estágio conta"));
    }

    @Test
    void semSucessoExoneracaoObrigatoria() {
        var e = estagio(false);
        service.concluir(pessoa, e.getId(), PeriodoProva.Avaliacao.NEGATIVA, "Nao revelou", LocalDate.of(2026, 9, 1), null);
        verify(cessacaoService).cessar(eq(pessoa), any(), eq(LocalDate.of(2026, 9, 1)), eq("EXONERACAO_OBRIGATORIA"), anyString());
    }

    @Test
    void quemVemDeOutraCarreiraRegressaSemCessar() {
        var e = estagio(true);
        var r = service.cessarAntecipadamente(pessoa, e.getId(), "Nao revela as competencias", LocalDate.of(2026, 3, 1), null);
        verify(cessacaoService, never()).cessar(any(), any(), any(), any(), any());
        assertTrue(r.alertas().get(0).contains("regressa à carreira"));
    }

    @Test
    void termoCertoSemContratoRegistadoNaoAbrePeriodoExperimental() {
        when(contratoRepository.findCurrentByFuncionarioId(pessoa)).thenReturn(Optional.empty());
        assertEquals(422, assertThrows(IgrpResponseStatusException.class, () -> service.registar(pessoa,
                ModalidadeProvimento.CONTRATO_TERMO_CERTO, null, null, POSSE, null, false, null, null, null)).getStatusCode().value());
    }
}
