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

import cv.igrp.RH_Service.colaboradores.application.commands.MudarEstadoColaboradorCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.MudarEstadoColaboradorCommandHandler;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.AposentacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-APO-05..12: condicoes de cada modalidade, desligacao, aposentacao (cessacao, Lugar extinto, facto). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AposentacaoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 25);

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private AposentacaoRepository aposentacaoRepository;
    @Mock private AntiguidadeService antiguidadeService;
    @Mock private QuemEstaNoServico quemEstaNoServico;
    @Mock private CessacaoService cessacaoService;
    @Mock private MudarEstadoColaboradorCommandHandler mudarEstado;
    @Mock private WorkerStateRepository workerStateRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private DiarioFactos diarioFactos;
    @Mock private NotificacaoRepository notificacaoRepository;

    private AposentacaoService service;
    private final FuncionarioId id = FuncionarioId.gerarNovo();
    private Funcionario pessoa;

    @BeforeEach
    void setUp() {
        service = new AposentacaoService(funcionarioRepository, aposentacaoRepository, antiguidadeService, quemEstaNoServico,
                cessacaoService, mudarEstado, workerStateRepository, assignmentRepository, positionRepository, diarioFactos,
                new Notificador(notificacaoRepository)) {
            @Override LocalDate hoje() { return HOJE; }
        };
        pessoa = mock(Funcionario.class);
        when(pessoa.getId()).thenReturn(id);
        when(pessoa.getIsActive()).thenReturn(true);
        when(pessoa.getNomeCompleto()).thenReturn("Maria Lopes");
        when(pessoa.getDataAdmissao()).thenReturn(LocalDate.of(1995, 1, 2));
        when(pessoa.getDataNascimento()).thenReturn(LocalDate.of(1966, 5, 20));   // 60 anos
        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(pessoa));
        when(aposentacaoRepository.findProcessos(id)).thenReturn(List.of());
        when(aposentacaoRepository.findProrrogacoes(id)).thenReturn(List.of());
        when(aposentacaoRepository.save(any(ProcessoAposentacao.class))).thenAnswer(i -> i.getArgument(0));
        servico(31L * 365);
    }

    private void servico(long diasContados) {
        when(antiguidadeService.calcular(eq(id), any())).thenReturn(new CalculadoraAntiguidade.Antiguidade(
                LocalDate.of(1995, 1, 2), HOJE, diasContados, 0, diasContados, (int) (diasContados / 365), 0, 0, List.of()));
    }

    private static int status(Runnable r) {
        return assertThrows(IgrpResponseStatusException.class, r::run).getStatusCode().value();
    }

    @Test
    void antecipadaAPedidoExige34AnosDeServico() {
        var e = assertThrows(IgrpResponseStatusException.class, () -> service.abrir(id, ModalidadeAposentacao.ANTECIPADA_PEDIDO,
                ProcessoAposentacao.Iniciativa.FUNCIONARIO, null, null, false));
        assertEquals(422, e.getStatusCode().value());
        assertTrue(e.getBody().getTitle().contains("34 anos de serviço"), e.getBody().getTitle());
        servico(34L * 365);
        assertEquals(ModalidadeAposentacao.ANTECIPADA_PEDIDO, service.abrir(id, ModalidadeAposentacao.ANTECIPADA_PEDIDO,
                ProcessoAposentacao.Iniciativa.FUNCIONARIO, null, null, false).getModalidade());
    }

    @Test
    void preAposentacaoExige58AnosE30DeServico() {
        servico(29L * 365);
        assertEquals(422, status(() -> service.abrir(id, ModalidadeAposentacao.PRE_APOSENTACAO, null, null, null, false)));
        servico(30L * 365);
        assertEquals(EstadoEsperado.PEDIDO, EstadoEsperado.de(service.abrir(id, ModalidadeAposentacao.PRE_APOSENTACAO, null, null, null, false)));
    }

    @Test
    void limiteDeIdadeSoDepoisDos65() {
        assertEquals(422, status(() -> service.abrir(id, ModalidadeAposentacao.LIMITE_IDADE, null, null, null, false)));
        service.abrir(id, ModalidadeAposentacao.LIMITE_IDADE, null, LocalDate.of(2031, 5, 20), null, false);
    }

    @Test
    void oProprioSoPedeAntecipadaOuPre() {
        servico(40L * 365);
        assertEquals(422, status(() -> service.abrir(id, ModalidadeAposentacao.INVALIDEZ,
                ProcessoAposentacao.Iniciativa.FUNCIONARIO, null, null, false)));
    }

    @Test
    void umProcessoDeCadaVez() {
        var aberto = ProcessoAposentacao.abrir(id, ModalidadeAposentacao.INVALIDEZ, null, HOJE, null, null, false);
        when(aposentacaoRepository.findProcessos(id)).thenReturn(List.of(aberto));
        assertEquals(409, status(() -> service.abrir(id, ModalidadeAposentacao.INVALIDEZ, null, null, null, false)));
    }

    @Test
    void desligarPassaAInactividadeNoQuadroAguardandoAposentacao() {
        var p = ProcessoAposentacao.abrir(id, ModalidadeAposentacao.INVALIDEZ, null, HOJE, null, null, false);
        p.deferir("D-3", HOJE, null);
        when(aposentacaoRepository.findProcesso(p.getId())).thenReturn(Optional.of(p));
        var inactivo = estado(false);
        when(workerStateRepository.findBySituacao(SituacaoFuncional.INACTIVIDADE_NO_QUADRO)).thenReturn(Optional.of(inactivo));
        service.desligar(id, p.getId(), HOJE.plusDays(1), null, null);
        var c = ArgumentCaptor.forClass(MudarEstadoColaboradorCommand.class);
        verify(mudarEstado).handle(c.capture());
        assertEquals(inactivo.getId().getStringValor(), c.getValue().getRequest().getWorkerStateId());
        assertEquals("AGUARDA_APOSENTACAO", c.getValue().getRequest().getMotivoCkey());
    }

    @Test
    void aposentacaoAntecipadaCessaOVinculoEExtingueOLugar() {
        var p = ProcessoAposentacao.abrir(id, ModalidadeAposentacao.ANTECIPADA_PEDIDO, null, HOJE, null, null, false);
        p.deferir("D-9", HOJE, null);
        when(aposentacaoRepository.findProcesso(p.getId())).thenReturn(Optional.of(p));
        var aposentado = estado(true);
        when(workerStateRepository.findBySituacao(SituacaoFuncional.APOSENTACAO)).thenReturn(Optional.of(aposentado));
        UUID afectacao = UUID.randomUUID(), lugar = UUID.randomUUID();
        when(cessacaoService.cessar(eq(id), eq(aposentado), any(), anyString(), any()))
                .thenReturn(new CessacaoService.Cessacao(pessoa, aposentado, null, null, afectacao, HOJE));
        var a = mock(Assignment.class);
        when(a.getPositionId()).thenReturn(lugar);
        when(assignmentRepository.findById(any())).thenReturn(Optional.of(a));
        var position = mock(Position.class);
        when(positionRepository.findById(any())).thenReturn(Optional.of(position));

        service.concluir(id, p.getId(), HOJE.plusDays(30), null, null);
        verify(position).extinguir();
        verify(positionRepository).save(position);
        verify(diarioFactos).registar(eq(id), eq(TipoFactoRh.APOSENTACAO), eq(HOJE.plusDays(30)), anyString(), any(), anyString(), any());
    }

    @Test
    void semEstadoDeAposentacaoNoCatalogoNaoSeConclui() {
        var p = ProcessoAposentacao.abrir(id, ModalidadeAposentacao.INVALIDEZ, null, HOJE, null, null, false);
        p.deferir("D-3", HOJE, null);
        when(aposentacaoRepository.findProcesso(p.getId())).thenReturn(Optional.of(p));
        when(workerStateRepository.findBySituacao(SituacaoFuncional.APOSENTACAO)).thenReturn(Optional.empty());
        assertEquals(422, status(() -> service.concluir(id, p.getId(), HOJE, null, null)));
        verify(cessacaoService, never()).cessar(any(), any(), any(), any(), any());
    }

    @Test
    void situacaoDizQuandoFaz65EQuandoPodeAntecipar() {
        var s = service.situacao(id);
        assertEquals(LocalDate.of(2031, 5, 20), s.faz65());
        assertEquals(LocalDate.of(2031, 5, 20), s.limiteEfectivo());
        assertEquals(HOJE.plusDays(3L * 365), s.completa34Anos());
        assertEquals(60, s.idade());
    }

    private static WorkerState estado(boolean cessa) {
        var e = mock(WorkerState.class);
        when(e.getId()).thenReturn(WorkerStateId.from(UUID.randomUUID()));
        when(e.isEndsEmployment()).thenReturn(cessa);
        return e;
    }

    /** Ajuda de leitura: o estado do processo devolvido. */
    private enum EstadoEsperado {
        PEDIDO;
        static EstadoEsperado de(ProcessoAposentacao p) { return valueOf(p.getEstado().name()); }
    }
}
