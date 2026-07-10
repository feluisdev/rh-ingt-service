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
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapInterimFeedbackRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ObjectiveRevision;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapInterimFeedbackMapper;

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
class AcceptObjectiveRevisionCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapInterimFeedbackRepository feedbackRepository;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapInterimFeedbackMapper mapper;

    @Mock
    private CurrentEmployeeResolver currentEmployeeResolver;

    @InjectMocks
    private AcceptObjectiveRevisionCommandHandler handler;

    private List<IndividualObjective> buildObjectives() {
        return List.of(
                IndividualObjective.create("OBJ-1", "Descrição do objetivo 1", "Indicador 1", new BigDecimal("100"), new BigDecimal("40")),
                IndividualObjective.create("OBJ-2", "Descrição do objetivo 2", "Indicador 2", new BigDecimal("100"), new BigDecimal("30")),
                IndividualObjective.create("OBJ-3", "Descrição do objetivo 3", "Indicador 3", new BigDecimal("100"), new BigDecimal("30")));
    }

    /** Fase IN_PROGRESS — elegível para applyObjectiveRevision. */
    private SiadapEvaluation buildInProgressEvaluation() {
        SiadapEvaluation created = SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));
        return created.contractualizeObjectives(buildObjectives()).acceptObjectives();
    }

    /** Fase OPEN (não avançou para IN_PROGRESS) — deve falhar a guarda de fase de applyObjectiveRevision. */
    private SiadapEvaluation buildOpenEvaluation() {
        SiadapEvaluation created = SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));
        return created.contractualizeObjectives(buildObjectives());
    }

    private SiadapInterimFeedback buildFeedbackWithRevision(UUID evaluationId, UUID revisionId, String objectiveCode,
                                                             String newObjectiveSmart, AcceptanceStatus status) {
        ObjectiveRevision revision = ObjectiveRevision.create(
                revisionId, "Objetivo atual", "Justificação", newObjectiveSmart, status, objectiveCode, null);
        return SiadapInterimFeedback.create(evaluationId, null, null, null, null,
                List.of(), List.of(), List.of(revision));
    }

    @Test
    void acceptsRevisionAndPromotesObjectiveDescriptionAtomically() {
        SiadapEvaluation evaluation = buildInProgressEvaluation();
        UUID evalUuid = UUID.fromString(evaluation.getId().getStringValor());
        UUID revisionId = UUID.randomUUID();
        String newObjectiveSmart = "Objetivo revisto e aceite pelo avaliado";
        SiadapInterimFeedback feedback = buildFeedbackWithRevision(
                evalUuid, revisionId, "OBJ-1", newObjectiveSmart, AcceptanceStatus.PENDING_ACCEPTANCE);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluation.getEmployeeId()));
        when(feedbackRepository.findByEvaluationId(evalUuid)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any(SiadapInterimFeedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(SiadapInterimFeedback.class))).thenReturn(new SiadapInterimFeedbackDTO());

        AcceptObjectiveRevisionCommand command = new AcceptObjectiveRevisionCommand(
                evalUuid.toString(), revisionId.toString());

        ResponseEntity<SiadapInterimFeedbackDTO> response = handler.handle(command);

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<SiadapInterimFeedback> feedbackCaptor = ArgumentCaptor.forClass(SiadapInterimFeedback.class);
        verify(feedbackRepository, times(1)).save(feedbackCaptor.capture());

        ArgumentCaptor<SiadapEvaluation> evaluationCaptor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository, times(1)).save(evaluationCaptor.capture());

        ObjectiveRevision savedRevision = feedbackCaptor.getValue().getObjectiveRevisions().stream()
                .filter(r -> revisionId.equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(AcceptanceStatus.ACCEPTED, savedRevision.getApprovalStatus());

        IndividualObjective revisedObjective = evaluationCaptor.getValue().getObjectives().stream()
                .filter(o -> "OBJ-1".equals(o.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals(newObjectiveSmart, revisedObjective.getDescription());
        assertEquals("Indicador 1", revisedObjective.getIndicator());
        assertEquals(0, new BigDecimal("100").compareTo(revisedObjective.getTargetValue()));
        assertEquals(0, new BigDecimal("40").compareTo(revisedObjective.getWeight()));
    }

    @Test
    void throwsNotFoundWhenEvaluationDoesNotExist() {
        when(evaluationRepository.findById(any())).thenReturn(Optional.empty());

        AcceptObjectiveRevisionCommand command = new AcceptObjectiveRevisionCommand(
                UUID.randomUUID().toString(), UUID.randomUUID().toString());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(404, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void throwsForbiddenWhenCurrentUserIsNotTheEmployee() {
        SiadapEvaluation evaluation = buildInProgressEvaluation();

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.gerarNovo());

        AcceptObjectiveRevisionCommand command = new AcceptObjectiveRevisionCommand(
                evaluation.getId().getStringValor(), UUID.randomUUID().toString());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(403, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
        verify(evaluationRepository, never()).save(any());
    }

    // Pitfall 7: phase guard on applyObjectiveRevision must prevent BOTH saves, not just one —
    // acceptRevision() succeeds (revision is PENDING_ACCEPTANCE) but the evaluation is still OPEN,
    // so applyObjectiveRevision throws BEFORE either repository's save() is invoked.
    @Test
    void throwsBadRequestWhenEvaluationNotInEligiblePhase() {
        SiadapEvaluation evaluation = buildOpenEvaluation();
        UUID evalUuid = UUID.fromString(evaluation.getId().getStringValor());
        UUID revisionId = UUID.randomUUID();
        SiadapInterimFeedback feedback = buildFeedbackWithRevision(
                evalUuid, revisionId, "OBJ-1", "Objetivo revisto", AcceptanceStatus.PENDING_ACCEPTANCE);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluation.getEmployeeId()));
        when(feedbackRepository.findByEvaluationId(evalUuid)).thenReturn(Optional.of(feedback));

        AcceptObjectiveRevisionCommand command = new AcceptObjectiveRevisionCommand(
                evalUuid.toString(), revisionId.toString());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void throwsBadRequestWhenRevisionIsNotAcceptable() {
        SiadapEvaluation evaluation = buildInProgressEvaluation();
        UUID evalUuid = UUID.fromString(evaluation.getId().getStringValor());
        UUID revisionId = UUID.randomUUID();
        // Draft revision (approvalStatus == null) — acceptRevision()'s own guard must reject it.
        SiadapInterimFeedback feedback = buildFeedbackWithRevision(
                evalUuid, revisionId, "OBJ-1", "Objetivo revisto", null);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluation.getEmployeeId()));
        when(feedbackRepository.findByEvaluationId(evalUuid)).thenReturn(Optional.of(feedback));

        AcceptObjectiveRevisionCommand command = new AcceptObjectiveRevisionCommand(
                evalUuid.toString(), revisionId.toString());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
        verify(evaluationRepository, never()).save(any());
    }
}
