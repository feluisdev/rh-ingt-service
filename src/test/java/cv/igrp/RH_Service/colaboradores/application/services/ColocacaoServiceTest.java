package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

/** BR-AF-15 a BR-AF-22: a colocação de quem não tem Lugar — admissão e reingresso. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ColocacaoServiceTest {

    @Mock AssignmentService assignmentService;
    @Mock AssignmentRepository assignmentRepository;
    @Mock FuncionarioRepository funcionarioRepository;
    @Mock ContratoRepository contratoRepository;
    @Mock PositionRepository positionRepository;
    @Mock GradeRepository gradeRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock WorkerStateRepository workerStateRepository;
    @Mock HistoricoEstadoColaboradorRepository historicoRepository;
    @org.mockito.Mock private cv.igrp.RH_Service.colaboradores.application.services.DiarioFactos diarioFactos;
    @InjectMocks ColocacaoService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final UUID lugarId = UUID.randomUUID();
    private final UUID categoria = UUID.randomUUID();
    private final LocalDate admissao = LocalDate.of(2020, 1, 1);
    private final LocalDate inicio = LocalDate.of(2026, 11, 1);
    private final WorkerState activo = estado(SituacaoFuncional.ACTIVIDADE_NO_QUADRO);

    @BeforeEach
    void cenarioDeAdmissao() {
        funcionario(true, activo);
        when(workerStateRepository.findById(any())).thenReturn(Optional.of(activo));
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.empty());
        when(assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(funcionarioId)).thenReturn(List.of());
        contrato("ATIVO", admissao);
        when(positionRepository.findById(any())).thenReturn(Optional.of(lugar(categoria)));
        when(assignmentService.afectar(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> Assignment.criar(funcionarioId, lugarId, inv.getArgument(2), null,
                        TipoAfectacao.PRINCIPAL, inv.getArgument(4), inicio, null));
    }

    @Test
    void primeiraVezEAdmissao() {
        var r = colocar(null, null);
        assertEquals(Assignment.ADMISSAO, r.origem());
        verify(assignmentService).afectar(eq(funcionarioId), eq(lugarId), any(), any(), eq(Assignment.ADMISSAO), any(), eq(inicio), any());
    }

    @Test
    void quemTemLugarMudaPorUmMovimento() {
        Assignment corrente = anterior(null);
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.of(corrente));
        assert422(() -> colocar(null, null));
    }

    @Test
    void inactivoNaoSeColoca() {
        funcionario(false, activo);
        assert422(() -> colocar(null, null));
    }

    @Test
    void emInactividadeForaDoQuadroColocaSeNoRegresso() {
        when(workerStateRepository.findById(any())).thenReturn(Optional.of(estado(SituacaoFuncional.INACTIVIDADE_FORA_QUADRO)));
        assert422(() -> colocar(null, null));
    }

    @Test
    void semContratoActivoNaoHaColocacao() {
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.empty());
        assert422(() -> colocar(null, null));
        contrato("SUSPENSO", admissao);
        assert422(() -> colocar(null, null));
        contrato("ATIVO", inicio.plusDays(1));
        assert422(() -> colocar(null, null));
    }

    @Test
    void aOrigemEDoSistema() {
        assert422(() -> colocar("PROMOCAO", null));
        Assignment ant1 = anterior(inicio.minusMonths(2));
        when(assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(funcionarioId)).thenReturn(List.of(ant1));
        assert422(() -> colocar(Assignment.ADMISSAO, null));
        assertEquals(ColocacaoService.REINGRESSO, colocar(ColocacaoService.REINGRESSO, null).origem());
    }

    @Test
    void naoSeSobrepoeAUltimaAfectacao() {
        Assignment ant2 = anterior(inicio);
        when(assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(funcionarioId)).thenReturn(List.of(ant2));
        assert422(() -> colocar(null, null));
    }

    @Test
    void reingressoNoutraCategoriaERecusado() {
        Assignment ant3 = anterior(inicio.minusMonths(2));
        when(assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(funcionarioId)).thenReturn(List.of(ant3));
        when(positionRepository.findById(any())).thenReturn(Optional.of(lugar(UUID.randomUUID())));
        assert422(() -> colocar(null, null));
        verify(assignmentService, never()).afectar(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void reingressoDeDisponibilidadeMantemOEscalaoEVoltaAActividade() {
        WorkerState disponibilidade = estado(SituacaoFuncional.DISPONIBILIDADE);
        funcionario(true, disponibilidade);
        when(workerStateRepository.findById(any())).thenReturn(Optional.of(disponibilidade));
        when(workerStateRepository.findBySituacao(SituacaoFuncional.ACTIVIDADE_NO_QUADRO)).thenReturn(Optional.of(activo));
        Assignment ultima = anterior(inicio.minusMonths(2));
        when(assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(funcionarioId)).thenReturn(List.of(ultima));

        var r = colocar(null, null);

        assertEquals(ColocacaoService.REINGRESSO, r.origem());
        assertEquals(ultima.getGradeId(), r.afectacao().getGradeId(), "sem escalão indicado, mantém o que tinha");
        ArgumentCaptor<HistoricoEstadoColaborador> h = ArgumentCaptor.forClass(HistoricoEstadoColaborador.class);
        verify(historicoRepository).save(h.capture());
        assertEquals(activo.getId().getValor(), h.getValue().getEstadoNovoId());
        assertTrue(r.alertas().isEmpty());
    }

    @Test
    void disponibilidadeSemEstadoDeActividadeDaAlerta() {
        WorkerState disponibilidade = estado(SituacaoFuncional.DISPONIBILIDADE);
        funcionario(true, disponibilidade);
        when(workerStateRepository.findById(any())).thenReturn(Optional.of(disponibilidade));
        when(workerStateRepository.findBySituacao(SituacaoFuncional.ACTIVIDADE_NO_QUADRO)).thenReturn(Optional.empty());
        Assignment ant4 = anterior(inicio.minusMonths(2));
        when(assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(funcionarioId)).thenReturn(List.of(ant4));

        assertEquals(1, colocar(null, null).alertas().size());
        verify(historicoRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ apoio

    private ColocacaoService.Resultado colocar(String origem, UUID grade) {
        return service.colocar(funcionarioId, lugarId, grade, null, origem, TipoAfectacao.PRINCIPAL, inicio, null);
    }

    private static void assert422(org.junit.jupiter.api.function.Executable e) {
        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class, e);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getStatusCode().value());
    }

    private void funcionario(boolean activoFlag, WorkerState estado) {
        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(Funcionario.reconstituir(
                funcionarioId, "F000001", "Teste", LocalDate.of(1990, 1, 1), "F", "SOLTEIRO", "123", null, null,
                null, null, "CV", null, null, null, null, null, null, estado.getId().getValor(), admissao, activoFlag)));
    }

    private void contrato(String status, LocalDate inicioContrato) {
        when(contratoRepository.findCurrentByFuncionarioId(funcionarioId)).thenReturn(Optional.of(Contrato.reconstituir(
                ContratoId.gerarNovo(), funcionarioId, UUID.randomUUID(), "C-1", inicioContrato, null, null, true,
                status, 0, "TEMPO_COMPLETO", null, null, null)));
    }

    /** Última afectação principal, já encerrada em {@code fim}, num escalão da {@link #categoria}. */
    private Assignment anterior(LocalDate fim) {
        Grade g = Grade.reconstituir(GradeId.gerarNovo(), CategoryId.from(categoria), 2, "E2", "Escalão 2",
                BigDecimal.TEN, null, true);
        when(gradeRepository.findById(g.getId())).thenReturn(Optional.of(g));
        return Assignment.reconstituir(AssignmentId.gerarNovo(), funcionarioId, UUID.randomUUID(), g.getId().getValor(),
                null, TipoAfectacao.PRINCIPAL.name(), Assignment.ADMISSAO, null, admissao, fim, fim == null, true, null);
    }

    private Position lugar(UUID cat) {
        return Position.reconstituir(PositionId.from(lugarId), "L-01", UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), cat, null, null, Position.ATIVO, null, true);
    }

    private static WorkerState estado(SituacaoFuncional s) {
        return WorkerState.reconstruir(WorkerStateId.gerarNovo(), s.name(), s.name(), false, true, false, s);
    }
}
