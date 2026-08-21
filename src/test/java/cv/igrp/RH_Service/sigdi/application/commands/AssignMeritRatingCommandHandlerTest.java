package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.config.SiadapCcaSecurityProperties;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.CompetencyCategory;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.SiadapMeritRating;
import cv.igrp.RH_Service.sigdi.application.dto.AssignMeritRatingRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyItem;
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
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * ACH-A-04 (SIA-01): correcting the merit rating of an already finalized SIADAP evaluation.
 * Covers the happy path, a missing evaluation, an invalid mention code, and the phase boundary
 * the handler enforces (only HARMONIZATION and CLOSED may be written).
 */
@ExtendWith(MockitoExtension.class)
class AssignMeritRatingCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapEvaluationMapper mapper;

    @Mock
    private CurrentEmployeeResolver currentEmployeeResolver;

    @Mock
    private SiadapCcaSecurityProperties ccaSecurityProperties;

    @InjectMocks
    private AssignMeritRatingCommandHandler handler;

    @Captor
    private ArgumentCaptor<SiadapEvaluation> savedCaptor;

    /**
     * Builds an evaluation in the requested phase, already carrying a computed final score and
     * the mention derived from it — the state a finalized evaluation is in when someone spots
     * that the mention is wrong.
     */
    private SiadapEvaluation buildEvaluation(EvaluationPhase phase, SiadapMeritRating currentRating) {
        IndividualObjective objective = IndividualObjective.reconstruct(
                "OBJ-1", "Descrição do objetivo 1", "Indicador 1",
                new BigDecimal("100"), new BigDecimal("100"), 3, new BigDecimal("100"));
        CompetencyItem competency = CompetencyItem.reconstruct(
                "COMP-1", "Competência 1", CompetencyCategory.BEHAVIORAL, 3);

        return SiadapEvaluation.reconstruct(
                SiadapEvaluationId.gerarNovo(),
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                List.of(objective),
                List.of(competency),
                new BigDecimal("60"), new BigDecimal("40"),
                null, new BigDecimal("3.00"), currentRating,
                EvaluationPhase.CLOSED.equals(phase),
                phase, AcceptanceStatus.ACCEPTED, null);
    }

    private AssignMeritRatingCommand commandFor(SiadapEvaluation evaluation, String rating) {
        return new AssignMeritRatingCommand(
                evaluation.getId().getStringValor(), new AssignMeritRatingRequestDTO(rating));
    }

    @Test
    void assignsMeritRatingOnAClosedEvaluation() {
        SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.CLOSED, SiadapMeritRating.REGULAR);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toFullDto(any(SiadapEvaluation.class))).thenReturn(new SiadapEvaluationDTO());
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.gerarNovo());
        when(ccaSecurityProperties.isCca(any())).thenReturn(true);

        ResponseEntity<SiadapEvaluationDTO> response =
                handler.handle(commandFor(evaluation, SiadapMeritRating.VERY_GOOD.getCode()));

        assertEquals(200, response.getStatusCode().value());
        verify(evaluationRepository, times(1)).save(savedCaptor.capture());

        SiadapEvaluation saved = savedCaptor.getValue();
        assertEquals(SiadapMeritRating.VERY_GOOD, saved.getMeritRating());
        // The correction touches the mention only: phase, final score and quota flag survive.
        assertEquals(EvaluationPhase.CLOSED, saved.getPhase());
        assertEquals(new BigDecimal("3.00"), saved.getFinalScore());
        assertTrue(saved.isValidatedQuota());
    }

    @Test
    void assignsMeritRatingOnAnEvaluationInHarmonization() {
        SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.HARMONIZATION, SiadapMeritRating.GOOD);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toFullDto(any(SiadapEvaluation.class))).thenReturn(new SiadapEvaluationDTO());
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.gerarNovo());
        when(ccaSecurityProperties.isCca(any())).thenReturn(true);

        ResponseEntity<SiadapEvaluationDTO> response =
                handler.handle(commandFor(evaluation, SiadapMeritRating.EXCELLENT.getCode()));

        assertEquals(200, response.getStatusCode().value());
        verify(evaluationRepository, times(1)).save(savedCaptor.capture());
        assertEquals(SiadapMeritRating.EXCELLENT, savedCaptor.getValue().getMeritRating());
    }

    @Test
    void throwsNotFoundWhenEvaluationDoesNotExist() {
        when(evaluationRepository.findById(any())).thenReturn(Optional.empty());

        AssignMeritRatingCommand command = new AssignMeritRatingCommand(
                UUID.randomUUID().toString(),
                new AssignMeritRatingRequestDTO(SiadapMeritRating.GOOD.getCode()));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(404, exception.getBody().getStatus());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void throwsBadRequestAndListsAllowedValuesForAnInvalidMeritRating() {
        AssignMeritRatingCommand command = new AssignMeritRatingCommand(
                UUID.randomUUID().toString(), new AssignMeritRatingRequestDTO("OTIMO"));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        String title = exception.getBody().getTitle();
        assertTrue(title.contains("OTIMO"), "A mensagem deve repetir o valor recebido: " + title);
        for (SiadapMeritRating allowed : SiadapMeritRating.values())
            assertTrue(title.contains(allowed.getCode()),
                    "A mensagem deve listar " + allowed.getCode() + ": " + title);

        verify(evaluationRepository, never()).save(any());
    }

    /**
     * Boundary the handler owns: before finalization there is no final score, and
     * finalizeEvaluation() would recompute the mention anyway, so the write is refused.
     */
    @Test
    void refusesToAssignMeritRatingBeforeTheEvaluationIsFinalized() {
        SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.MANAGER_EVALUATION, null);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.gerarNovo());
        when(ccaSecurityProperties.isCca(any())).thenReturn(true);

        AssignMeritRatingCommand command = commandFor(evaluation, SiadapMeritRating.EXCELLENT.getCode());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        verify(evaluationRepository, never()).save(any());
    }
}
