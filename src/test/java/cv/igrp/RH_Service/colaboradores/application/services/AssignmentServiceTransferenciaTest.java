package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.OrgFunction;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link AssignmentService#transferir}: muda de cadeira mantendo a posição na grelha
 * (carreira, categoria e escalão) e trata a função exercida de forma explícita.
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceTransferenciaTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private FunctionRepository functionRepository;

    @org.mockito.Mock private cv.igrp.RH_Service.colaboradores.application.services.DiarioFactos diarioFactos;
    @InjectMocks private AssignmentService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final UUID origemId = UUID.randomUUID();
    private final UUID destinoId = UUID.randomUUID();
    private final UUID careerId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID gradeId = UUID.randomUUID();
    private final UUID jobOrigem = UUID.randomUUID();
    private final UUID jobDestino = UUID.randomUUID();
    private final LocalDate inicio = LocalDate.of(2024, 1, 1);
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 1);

    private Assignment afectacaoActual(UUID functionId) {
        return Assignment.reconstituir(AssignmentId.gerarNovo(), funcionarioId, origemId, gradeId,
                functionId, TipoAfectacao.PRINCIPAL.name(), Assignment.ADMISSAO, null, inicio, null, true, true, null);
    }

    private Position lugar(UUID id, UUID jobId, UUID career, UUID category, String estado) {
        return Position.reconstituir(PositionId.from(id), "L-" + id.toString().substring(0, 4), jobId,
                UUID.randomUUID(), career, category, null, null, estado, null, true);
    }

    private void origemEDestino(Position destino) {
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, careerId, categoryId, Position.ATIVO)));
        when(positionRepository.findById(PositionId.from(destinoId))).thenReturn(Optional.of(destino));
    }

    @Test
    void transfereMantendoEscalaoEFuncaoCompativel() {
        UUID functionId = UUID.randomUUID();
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(functionId)));
        origemEDestino(lugar(destinoId, jobDestino, careerId, categoryId, Position.ATIVO));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(false);
        // Função genérica (jobId nulo) serve qualquer cargo.
        when(functionRepository.findById(FunctionId.from(functionId)))
                .thenReturn(Optional.of(OrgFunction.reconstruir(FunctionId.from(functionId), "F1",
                        "Função genérica", null, null, true)));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = service.transferir(funcionarioId, destinoId, null, dataEfeito, "Transferência");

        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(captor.capture());
        Assignment fechada = captor.getAllValues().get(0);
        Assignment nova = captor.getAllValues().get(1);

        assertFalse(fechada.getIsCurrent());
        assertEquals(dataEfeito.minusDays(1), fechada.getDataFim());
        assertEquals(destinoId, nova.getPositionId());
        assertEquals(gradeId, nova.getGradeId());
        assertEquals(functionId, nova.getFunctionId());
        assertEquals(Assignment.TRANSFERENCIA, nova.getOrigem());
        assertEquals(destinoId, resultado.lugarNovo().getId().getValor());
        assertEquals(origemId, resultado.lugarAnterior().getId().getValor());
    }

    @Test
    void exigeFuncaoQuandoAActualNaoServeOCargoDoDestino() {
        UUID functionId = UUID.randomUUID();
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(functionId)));
        origemEDestino(lugar(destinoId, jobDestino, careerId, categoryId, Position.ATIVO));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(false);
        when(functionRepository.findById(FunctionId.from(functionId)))
                .thenReturn(Optional.of(OrgFunction.reconstruir(FunctionId.from(functionId), "F1",
                        "Função do cargo de origem", null, jobOrigem, true)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.transferir(funcionarioId, destinoId, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void aceitaFuncaoIndicadaCompativelComOCargoDoDestino() {
        UUID functionActual = UUID.randomUUID();
        UUID functionNova = UUID.randomUUID();
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(functionActual)));
        origemEDestino(lugar(destinoId, jobDestino, careerId, categoryId, Position.ATIVO));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(false);
        when(functionRepository.findById(FunctionId.from(functionNova)))
                .thenReturn(Optional.of(OrgFunction.reconstruir(FunctionId.from(functionNova), "F2",
                        "Função do destino", null, jobDestino, true)));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.transferir(funcionarioId, destinoId, functionNova, dataEfeito, null);

        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(captor.capture());
        assertEquals(functionNova, captor.getAllValues().get(1).getFunctionId());
    }

    @Test
    void recusaLugarDeDestinoDeOutraCategoria() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        origemEDestino(lugar(destinoId, jobDestino, careerId, UUID.randomUUID(), Position.ATIVO));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(false);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.transferir(funcionarioId, destinoId, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaLugarDeDestinoOcupado() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        origemEDestino(lugar(destinoId, jobDestino, careerId, categoryId, Position.ATIVO));
        when(assignmentRepository.temTitular(destinoId)).thenReturn(true);

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.transferir(funcionarioId, destinoId, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaLugarDeDestinoCongelado() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        origemEDestino(lugar(destinoId, jobDestino, careerId, categoryId, Position.CONGELADO));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.transferir(funcionarioId, destinoId, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaTransferenciaParaOMesmoLugar() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));
        when(positionRepository.findById(PositionId.from(origemId)))
                .thenReturn(Optional.of(lugar(origemId, jobOrigem, careerId, categoryId, Position.ATIVO)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.transferir(funcionarioId, origemId, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaDataDeEfeitoNaoPosteriorAoInicioDaAfectacao() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoActual(null)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.transferir(funcionarioId, destinoId, null, inicio, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void recusaSemAfectacaoCorrente() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.transferir(funcionarioId, destinoId, null, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
    }
}
