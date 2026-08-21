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
import cv.igrp.RH_Service.sigdi.application.dto.RecordObjectiveAchievementRequestDTO;
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

@ExtendWith(MockitoExtension.class)
class RecordObjectiveAchievementCommandHandlerTest {

    private static final Integer YEAR = 2026;
    private static final String OBJECTIVE_CODE = "OBJ-1";

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapEvaluationMapper mapper;

    @Mock
    private CurrentEmployeeResolver currentEmployeeResolver;

    @InjectMocks
    private RecordObjectiveAchievementCommandHandler handler;

    // MANAGER_EVALUATION: uma das duas fases em que recordObjectiveAchievement() é permitido
    // (a outra é IN_PROGRESS). O objetivo tem de coincidir com o código do pedido, senão o
    // domínio lança 400 "Objetivo não encontrado" e o caminho feliz não prova nada.
    private SiadapEvaluation buildEvaluation(SiadapEvaluationId id) {
        IndividualObjective objective = IndividualObjective.create(
                OBJECTIVE_CODE, "Descrição do objetivo 1", "Indicador 1", new BigDecimal("100"), new BigDecimal("100"));

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
                EvaluationPhase.MANAGER_EVALUATION, AcceptanceStatus.ACCEPTED, null);
    }

    private RecordObjectiveAchievementCommand buildCommand(String evaluationId) {
        RecordObjectiveAchievementRequestDTO body = new RecordObjectiveAchievementRequestDTO(
                evaluationId, OBJECTIVE_CODE, new BigDecimal("80"), 3);
        return new RecordObjectiveAchievementCommand(body);
    }

    @Test
    void recordsAchievementWhenCurrentUserIsTheEvaluator() {
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

        RecordObjectiveAchievementCommand command = buildCommand(evalId.getStringValor());

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

        RecordObjectiveAchievementCommand command = buildCommand(evalId.getStringValor());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(404, exception.getBody().getStatus());

        verify(evaluationRepository, never()).save(any());
        verify(currentEmployeeResolver, never()).resolve();
    }

    // WR-01: only the avaliador desta avaliação pode registar a execução dos objetivos.
    @Test
    void throwsForbiddenWhenCurrentUserIsNotTheEvaluator() {
        SiadapEvaluationId evalId = SiadapEvaluationId.gerarNovo();
        SiadapEvaluation evaluation = buildEvaluation(evalId);

        when(evaluationRepository.findById(any(SiadapEvaluationId.class)))
                .thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve())
                .thenReturn(FuncionarioId.gerarNovo());

        RecordObjectiveAchievementCommand command = buildCommand(evalId.getStringValor());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(403, exception.getBody().getStatus());

        verify(evaluationRepository, never()).save(any());
    }
}
