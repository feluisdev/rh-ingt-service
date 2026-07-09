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
import cv.igrp.RH_Service.sigdi.application.dto.ObjectiveRevisionDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapInterimFeedbackRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ObjectiveRevision;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapInterimFeedbackMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * CR-01/CR-02/WR-02 regression coverage: the generic "Guardar Feedback Intercalar" endpoint must
 * (a) be actor-gated like the 3 dedicated propose/accept/negotiate endpoints, (b) never let a
 * client drive {@code approvalStatus}/{@code lastNegotiationComment} directly for an existing
 * revision, and (c) never accept a revision {@code id} that doesn't belong to this evaluation's
 * own persisted feedback.
 *
 * <p>Uses a real {@link SiadapInterimFeedbackMapper} (stateless, no dependencies) rather than a
 * mock, so the DTO→domain parsing and the handler's reconciliation logic are exercised together,
 * exactly as in production.
 */
@ExtendWith(MockitoExtension.class)
class SaveSiadapInterimFeedbackCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapInterimFeedbackRepository feedbackRepository;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private CurrentEmployeeResolver currentEmployeeResolver;

    private final SiadapInterimFeedbackMapper mapper = new SiadapInterimFeedbackMapper();

    private SaveSiadapInterimFeedbackCommandHandler handler;

    private SaveSiadapInterimFeedbackCommandHandler handler() {
        return new SaveSiadapInterimFeedbackCommandHandler(
                feedbackRepository, evaluationRepository, mapper, currentEmployeeResolver);
    }

    private SiadapEvaluation buildEvaluation(String employeeId, String evaluatorId) {
        return SiadapEvaluation.create(employeeId, YEAR, UUID.randomUUID().toString(), evaluatorId,
                new BigDecimal("60"), new BigDecimal("40"));
    }

    @Test
    void throwsForbiddenWhenCurrentUserIsNeitherEvaluatorNorEmployee() {
        handler = handler();
        String employeeId = UUID.randomUUID().toString();
        String evaluatorId = UUID.randomUUID().toString();
        SiadapEvaluation evaluation = buildEvaluation(employeeId, evaluatorId);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.gerarNovo());

        SiadapInterimFeedbackDTO body = new SiadapInterimFeedbackDTO(
                evaluation.getId().getStringValor(), null, null, null, null, List.of(), List.of(), List.of());
        SaveSiadapInterimFeedbackCommand command = new SaveSiadapInterimFeedbackCommand(
                evaluation.getId().getStringValor(), body);

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(403, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void savePreservesExistingApprovalStatusIgnoringClientSmuggledValue() {
        handler = handler();
        String employeeId = UUID.randomUUID().toString();
        String evaluatorId = UUID.randomUUID().toString();
        SiadapEvaluation evaluation = buildEvaluation(employeeId, evaluatorId);
        UUID evalUuid = UUID.fromString(evaluation.getId().getStringValor());
        UUID revisionId = UUID.randomUUID();

        ObjectiveRevision existingRevision = ObjectiveRevision.create(
                revisionId, "Objetivo atual", "Justificação", "Novo objetivo SMART",
                AcceptanceStatus.PENDING_ACCEPTANCE, "OBJ-1", null);
        SiadapInterimFeedback existingFeedback = SiadapInterimFeedback.create(
                evalUuid, null, null, null, null, List.of(), List.of(), List.of(existingRevision));

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluatorId));
        when(feedbackRepository.findByEvaluationId(evalUuid)).thenReturn(Optional.of(existingFeedback));
        when(feedbackRepository.save(any(SiadapInterimFeedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Client attempts to smuggle approvalStatus: "ACCEPTED" directly, bypassing acceptRevision().
        ObjectiveRevisionDTO revisionDto = new ObjectiveRevisionDTO(
                revisionId.toString(), "Objetivo atual (editado)", "Justificação", "Novo objetivo SMART",
                "ACCEPTED", "OBJ-1", "Comentário smuggle");
        SiadapInterimFeedbackDTO body = new SiadapInterimFeedbackDTO(
                evalUuid.toString(), null, null, null, null, List.of(), List.of(), List.of(revisionDto));

        SaveSiadapInterimFeedbackCommand command = new SaveSiadapInterimFeedbackCommand(evalUuid.toString(), body);

        ResponseEntity<SiadapInterimFeedbackDTO> response = handler.handle(command);

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<SiadapInterimFeedback> captor = ArgumentCaptor.forClass(SiadapInterimFeedback.class);
        verify(feedbackRepository, times(1)).save(captor.capture());

        ObjectiveRevision saved = captor.getValue().getObjectiveRevisions().stream()
                .filter(r -> revisionId.equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(AcceptanceStatus.PENDING_ACCEPTANCE, saved.getApprovalStatus());
        assertEquals(null, saved.getLastNegotiationComment());
    }

    @Test
    void rejectsRevisionIdNotBelongingToThisEvaluation() {
        handler = handler();
        String employeeId = UUID.randomUUID().toString();
        String evaluatorId = UUID.randomUUID().toString();
        SiadapEvaluation evaluation = buildEvaluation(employeeId, evaluatorId);
        UUID evalUuid = UUID.fromString(evaluation.getId().getStringValor());
        // Belongs to a different evaluation's feedback — never persisted for this one.
        UUID foreignRevisionId = UUID.randomUUID();

        SiadapInterimFeedback existingFeedback = SiadapInterimFeedback.create(
                evalUuid, null, null, null, null, List.of(), List.of(), List.of());

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluatorId));
        when(feedbackRepository.findByEvaluationId(evalUuid)).thenReturn(Optional.of(existingFeedback));

        ObjectiveRevisionDTO revisionDto = new ObjectiveRevisionDTO(
                foreignRevisionId.toString(), "Objetivo atual", "Justificação", "Novo objetivo SMART",
                null, "OBJ-1", null);
        SiadapInterimFeedbackDTO body = new SiadapInterimFeedbackDTO(
                evalUuid.toString(), null, null, null, null, List.of(), List.of(), List.of(revisionDto));

        SaveSiadapInterimFeedbackCommand command = new SaveSiadapInterimFeedbackCommand(evalUuid.toString(), body);

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
    }

    /**
     * Regression coverage (re-review, 2026-07-09): a revision with NO client-supplied {@code id} —
     * the exact shape the frontend sends for a brand-new, never-saved draft row — must be accepted
     * and persisted. The previous fix round classified "genuinely new" by calling
     * {@code .getId()} on the already-mapped domain {@link ObjectiveRevision}, which is NEVER null
     * (the VO back-fills a random id in its constructor), so every new revision was wrongly rejected
     * as an "unknown foreign id" with a 400. This test fails against that regression and passes
     * against the fix (which classifies new-vs-existing from the raw DTO's id BEFORE mapping).
     */
    @Test
    void saveAcceptsNewRevisionWithoutClientSuppliedId() {
        handler = handler();
        String employeeId = UUID.randomUUID().toString();
        String evaluatorId = UUID.randomUUID().toString();
        SiadapEvaluation evaluation = buildEvaluation(employeeId, evaluatorId);
        UUID evalUuid = UUID.fromString(evaluation.getId().getStringValor());

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluatorId));
        when(feedbackRepository.findByEvaluationId(evalUuid)).thenReturn(Optional.empty());
        when(feedbackRepository.save(any(SiadapInterimFeedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Brand-new draft row — no `id` at all, exactly as the frontend sends it for a row the
        // user just added via "+ Adicionar Revisão" and has never saved before.
        ObjectiveRevisionDTO newRevisionDto = new ObjectiveRevisionDTO(
                null, "Objetivo atual", "Justificação", "Novo objetivo SMART",
                null, "OBJ-1", null);
        SiadapInterimFeedbackDTO body = new SiadapInterimFeedbackDTO(
                evalUuid.toString(), null, null, null, null, List.of(), List.of(), List.of(newRevisionDto));

        SaveSiadapInterimFeedbackCommand command = new SaveSiadapInterimFeedbackCommand(evalUuid.toString(), body);

        ResponseEntity<SiadapInterimFeedbackDTO> response = handler.handle(command);

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<SiadapInterimFeedback> captor = ArgumentCaptor.forClass(SiadapInterimFeedback.class);
        verify(feedbackRepository, times(1)).save(captor.capture());

        List<ObjectiveRevision> saved = captor.getValue().getObjectiveRevisions();
        assertEquals(1, saved.size());
        assertEquals("Objetivo atual", saved.get(0).getCurrentObjectiveText());
    }
}
