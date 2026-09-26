package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.PenaDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinarTest;
import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-DIS: a execução da pena — facto, cessação do vínculo, publicação, cessação (acessória) da comissão. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessoDisciplinarServiceTest {

    private static final LocalDate D = ProcessoDisciplinarTest.D;

    @Mock private ProcessoDisciplinarRepository repository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private CalendarioFeriadosService calendario;
    @Mock private DiarioFactos diarioFactos;
    @Mock private CessacaoService cessacaoService;
    @Mock private WorkerStateRepository workerStateRepository;
    @Mock private ComissaoServicoService comissaoServicoService;
    @Mock private LicencaMobilidadeRepository licencaRepository;
    @Mock private MobilidadeService mobilidadeService;
    @Mock private PublicacoesService publicacoes;
    @Mock private NotificacaoRepository notificacaoRepository;
    private ProcessoDisciplinarService service;
    private LocalDate hoje = D.plusDays(100);
    private final FuncionarioId arguido = FuncionarioId.gerarNovo();
    private final FuncionarioId instrutor = FuncionarioId.gerarNovo();

    @BeforeEach
    void setUp() {
        service = new ProcessoDisciplinarService(repository, funcionarioRepository, calendario, diarioFactos, cessacaoService,
                workerStateRepository, comissaoServicoService, licencaRepository, mobilidadeService, publicacoes,
                new Notificador(notificacaoRepository)) {
            @Override LocalDate hoje() { return hoje; }
        };
        var f = mock(Funcionario.class);
        when(f.getNomeCompleto()).thenReturn("Rui");
        when(f.getIsActive()).thenReturn(true);
        when(funcionarioRepository.findById(any())).thenReturn(Optional.of(f));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(calendario.feriadosDoColaborador(any(), any(), any())).thenReturn(Set.of());
        when(licencaRepository.findActiveByFuncionarioIdAt(any(), any())).thenReturn(List.of());
    }

    private ProcessoDisciplinar notificado(PenaDisciplinar pena, Integer duracao) {
        var p = ProcessoDisciplinarTest.decidido(arguido, instrutor, pena, duracao);
        when(repository.findById(p.getId())).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    void demissaoCessaOVinculoEPublica() {
        var p = notificado(PenaDisciplinar.DEMISSAO, null);
        var estado = mock(WorkerState.class);
        when(cessacaoService.estadoDeCessacaoPorOmissao()).thenReturn(estado);
        service.notificarDecisao(arguido, p.getId(), D.plusDays(40));
        verify(cessacaoService).cessar(eq(arguido), eq(estado), eq(D.plusDays(56)), eq("PENA_DISCIPLINAR"), anyString());
        verify(publicacoes).aPublicar(eq(PublicacaoOficial.TipoActo.PENA_DISCIPLINAR), any(), eq(arguido), anyString(), anyString(), anyString(), eq(D.plusDays(56)));
        verify(diarioFactos).registar(eq(arguido), eq(TipoFactoRh.PENA_DISCIPLINAR), eq(D.plusDays(56)), anyString(), any(), anyString(), any());
        assertNotNull(p.getEfeitosAplicadosEm());
    }

    @Test
    void suspensaoDeDirigenteCessaAComissaoPorAcessoria() {
        var p = notificado(PenaDisciplinar.SUSPENSAO, 30);
        var comissao = LicencaMobilidade.criar(arguido, SubtipoLicencaMobilidadeId.gerarNovo(), D.minusYears(1), D.plusYears(2), null, null,
                null, null, null, null, null);
        var subtipo = mock(SubtipoLicencaMobilidade.class);
        when(subtipo.regressaOuCessa()).thenReturn(true);
        when(licencaRepository.findActiveByFuncionarioIdAt(eq(arguido), any())).thenReturn(List.of(comissao));
        when(mobilidadeService.subtipoSeExistir(comissao)).thenReturn(Optional.of(subtipo));
        service.notificarDecisao(arguido, p.getId(), D.plusDays(40));
        verify(comissaoServicoService).cessar(eq(arguido), eq(comissao.getId()), eq(ComissaoServicoService.Iniciativa.PENA_DISCIPLINAR),
                eq(D.plusDays(41)), eq(D.plusDays(41)), anyString());
        verify(cessacaoService, never()).cessar(any(), any(), any(), any(), any());
        verify(publicacoes, never()).aPublicar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void naoExecutaAntesDoDia() {
        hoje = D.plusDays(40);
        var p = notificado(PenaDisciplinar.CENSURA_ESCRITA, null);
        service.notificarDecisao(arguido, p.getId(), D.plusDays(40));
        verify(diarioFactos, never()).registar(any(), any(), any(), any(), any(), any(), any());
        when(repository.findComPenaPorExecutar()).thenReturn(List.of(p));
        assertEquals(0, service.executarDevidas(D.plusDays(40)));
        assertEquals(1, service.executarDevidas(D.plusDays(41)));
    }

    @Test
    void novaPunicaoCaducaASuspensaoDaPenaAnterior() {
        var suspensa = ProcessoDisciplinarTest.instruido(arguido, instrutor, PenaDisciplinar.MULTA);
        suspensa.acusar(D.plusDays(20), PenaDisciplinar.MULTA, "A");
        suspensa.notificarAcusacao(D.plusDays(21), 10, false);
        suspensa.registarDefesa(D.plusDays(22), "D");
        suspensa.relatorio(D.plusDays(30), PenaDisciplinar.MULTA, 5, "R", D.plusDays(30));
        suspensa.decidir(D.plusDays(35), PenaDisciplinar.MULTA, 5, "D", null, false, 2);
        suspensa.notificarDecisao(D.plusDays(40));
        var nova = notificado(PenaDisciplinar.CENSURA_ESCRITA, null);
        when(repository.findAllByFuncionarioId(arguido)).thenReturn(List.of(suspensa, nova));
        service.notificarDecisao(arguido, nova.getId(), D.plusDays(90));
        assertNotNull(suspensa.getEfeitosAplicadosEm());
        verify(diarioFactos).registar(eq(arguido), eq(TipoFactoRh.PENA_DISCIPLINAR), eq(D.plusDays(91)), anyString(),
                eq(suspensa.getId().getStringValor()), anyString(), any());
    }

    @Test
    void numeroPorOmissaoEAlertaDePrescricao() {
        hoje = D;
        when(repository.contarDoAno(2026)).thenReturn(4L);
        var r = service.participar(arguido, null, null, D.minusMonths(7), D, "Factos", PenaDisciplinar.CENSURA_ESCRITA);
        assertEquals("PD/2026/005", r.processo().getProcessNumber());
        assertEquals(1, r.alertas().stream().filter(a -> a.contains("prescreve")).count());
    }
}
