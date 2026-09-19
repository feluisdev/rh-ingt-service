package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link AssignmentService#progredir}: sobe para o escalão activo seguinte da
 * categoria do Lugar, mantendo o Lugar e a função, e versiona a afectação (SCD Type 2).
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceProgressaoTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private FunctionRepository functionRepository;

    @InjectMocks private AssignmentService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final UUID positionId = UUID.randomUUID();
    private final UUID functionId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final LocalDate inicio = LocalDate.of(2024, 1, 1);
    private final LocalDate dataEfeito = LocalDate.of(2026, 10, 1);

    private Grade escalao(int numero, boolean activo) {
        return Grade.reconstituir(GradeId.gerarNovo(), CategoryId.from(categoryId), numero,
                "E" + numero, "Escalão " + numero, BigDecimal.valueOf(100 + numero), null, activo);
    }

    private Assignment afectacao(UUID gradeId) {
        return Assignment.reconstituir(AssignmentId.gerarNovo(), funcionarioId, positionId, gradeId,
                functionId, TipoAfectacao.PRINCIPAL.name(), Assignment.ADMISSAO, null, inicio, null, true, true, null);
    }

    private Position lugar(UUID careerId, UUID catId) {
        return Position.reconstituir(PositionId.from(positionId), "L-01", UUID.randomUUID(),
                UUID.randomUUID(), careerId, catId, null, null, Position.ATIVO, null, true);
    }

    @Test
    void progrideParaOEscalaoActivoSeguinteNoMesmoLugar() {
        Grade e1 = escalao(1, true);
        Grade e2Inactivo = escalao(2, false);
        Grade e3 = escalao(3, true);
        Assignment atual = afectacao(e1.getId().getValor());

        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.of(atual));
        when(positionRepository.findById(any())).thenReturn(Optional.of(lugar(UUID.randomUUID(), categoryId)));
        when(gradeRepository.findById(e1.getId())).thenReturn(Optional.of(e1));
        when(gradeRepository.findByCategoryIdOrderByGradeNumber(CategoryId.from(categoryId)))
                .thenReturn(List.of(e1, e2Inactivo, e3));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = service.progredir(funcionarioId, dataEfeito, "Progressão");

        ArgumentCaptor<Assignment> captor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(captor.capture());
        Assignment fechada = captor.getAllValues().get(0);
        Assignment nova = captor.getAllValues().get(1);

        assertFalse(fechada.getIsCurrent());
        assertEquals(dataEfeito.minusDays(1), fechada.getDataFim());

        assertTrue(nova.getIsCurrent());
        assertEquals(positionId, nova.getPositionId());
        assertEquals(functionId, nova.getFunctionId());
        assertEquals(e3.getId().getValor(), nova.getGradeId());
        assertEquals(Assignment.PROGRESSAO, nova.getOrigem());
        assertEquals(TipoAfectacao.PRINCIPAL, nova.getAssignmentType());
        assertEquals(dataEfeito, nova.getDataInicio());
        assertNull(nova.getOriginAssignmentId());

        assertEquals(e1, resultado.escalaoAnterior());
        assertEquals(e3, resultado.escalaoNovo());
    }

    @Test
    void falhaQuandoJaEstaNoUltimoEscalao() {
        Grade e1 = escalao(1, true);
        Grade e2 = escalao(2, true);
        Assignment atual = afectacao(e2.getId().getValor());

        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.of(atual));
        when(positionRepository.findById(any())).thenReturn(Optional.of(lugar(UUID.randomUUID(), categoryId)));
        when(gradeRepository.findById(e2.getId())).thenReturn(Optional.of(e2));
        when(gradeRepository.findByCategoryIdOrderByGradeNumber(CategoryId.from(categoryId)))
                .thenReturn(List.of(e1, e2));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.progredir(funcionarioId, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void falhaQuandoOLugarEstaForaDaGrelha() {
        Assignment atual = afectacao(null);

        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.of(atual));
        when(positionRepository.findById(any())).thenReturn(Optional.of(lugar(null, null)));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.progredir(funcionarioId, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void falhaSemAfectacaoCorrente() {
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.progredir(funcionarioId, dataEfeito, null));

        assertEquals(422, ex.getStatusCode().value());
    }

    @Test
    void falhaQuandoADataDeEfeitoNaoEPosteriorAoInicioDaAfectacao() {
        Assignment atual = afectacao(UUID.randomUUID());
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)).thenReturn(Optional.of(atual));

        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.progredir(funcionarioId, inicio, null));

        assertEquals(422, ex.getStatusCode().value());
        verify(assignmentRepository, never()).save(any());
    }
}
