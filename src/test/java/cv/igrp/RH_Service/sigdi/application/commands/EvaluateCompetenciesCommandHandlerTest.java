package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.dto.CompetencyItemDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EvaluateCompetenciesRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * EvaluateCompetenciesCommandHandler invoca {@code SiadapEvaluation.setCompetencies(List)} —
 * não o método de competência única com guarda de fase. {@code setCompetencies} não tem
 * guarda de fase, exige apenas lista não vazia com pelo menos uma competência BEHAVIORAL.
 * A fase da fixture é por isso indiferente ao resultado; o que decide o caminho feliz é o
 * pedido trazer uma competência dessa categoria.
 */
@ExtendWith(MockitoExtension.class)
class EvaluateCompetenciesCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapEvaluationMapper mapper;

    @Mock
    private CurrentEmployeeResolver currentEmployeeResolver;

    @InjectMocks
    private EvaluateCompetenciesCommandHandler handler;

    // Fase escolhida por coerência com o resto da suite (MANAGER_EVALUATION) — setCompetencies()
    // não consulta a fase, por isso esta escolha não é uma guarda que este teste esteja a exercer.
    private SiadapEvaluation buildEvaluation(SiadapEvaluationId id) {
        IndividualObjective objective = IndividualObjective.create(
                "OBJ-1", "Descrição do objetivo 1", "Indicador 1", new BigDecimal("100"), new BigDecimal("100"));

        return SiadapEvaluation.reconstruct(
                id,
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                List.of(objective),
                List.of(),
                new BigDecimal("60"), new BigDecimal("40"),
                null, null, null, false,
                EvaluationPhase.MANAGER_EVALUATION, AcceptanceStatus.ACCEPTED, null, false);
    }

    private EvaluateCompetenciesCommand buildCommand(String evaluationId) {
        CompetencyItemDTO behavioral = new CompetencyItemDTO("COMP-1", "Competência 1", "BEHAVIORAL", null);
        EvaluateCompetenciesRequestDTO body = new EvaluateCompetenciesRequestDTO(evaluationId, List.of(behavioral));
        return new EvaluateCompetenciesCommand(body);
    }

    @Test
    void evaluatesCompetenciesWhenCurrentUserIsTheEvaluator() {
        SiadapEvaluationId evalId = SiadapEvaluationId.gerarNovo();
        SiadapEvaluation evaluation = buildEvaluation(evalId);

        when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
                .thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toFullDto(any(SiadapEvaluation.class)))
                .thenReturn(new SiadapEvaluationDTO());
        when(currentEmployeeResolver.resolve())
                .thenReturn(FuncionarioId.from(evaluation.getEvaluatorId()));

        EvaluateCompetenciesCommand command = buildCommand(evalId.getStringValor());

        ResponseEntity<SiadapEvaluationDTO> response = handler.handle(command);

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository, times(1)).save(captor.capture());
    }

    @Test
    void throwsNotFoundWhenEvaluationDoesNotExist() {
        SiadapEvaluationId evalId = SiadapEvaluationId.gerarNovo();

        when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
                .thenReturn(Optional.empty());

        EvaluateCompetenciesCommand command = buildCommand(evalId.getStringValor());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(404, exception.getBody().getStatus());

        verify(evaluationRepository, never()).save(any());
        verify(currentEmployeeResolver, never()).resolve();
    }

    // WR-01: only the avaliador desta avaliação pode avaliar as competências.
    @Test
    void throwsForbiddenWhenCurrentUserIsNotTheEvaluator() {
        SiadapEvaluationId evalId = SiadapEvaluationId.gerarNovo();
        SiadapEvaluation evaluation = buildEvaluation(evalId);

        when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
                .thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve())
                .thenReturn(FuncionarioId.gerarNovo());

        EvaluateCompetenciesCommand command = buildCommand(evalId.getStringValor());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(403, exception.getBody().getStatus());

        verify(evaluationRepository, never()).save(any());
    }
}
