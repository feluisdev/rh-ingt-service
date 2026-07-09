package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import cv.igrp.RH_Service.sigdi.application.dto.NegotiateObjectiveRevisionRequestDTO;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class NegotiateObjectiveRevisionCommandHandlerTest {

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
    private NegotiateObjectiveRevisionCommandHandler handler;

    private SiadapEvaluation buildEvaluation() {
        return SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));
    }

    private SiadapInterimFeedback buildFeedbackWithPendingRevision(UUID evaluationId, UUID revisionId) {
        ObjectiveRevision pending = ObjectiveRevision.create(
                revisionId, "Objetivo atual", "Justificação", "Novo objetivo SMART",
                AcceptanceStatus.PENDING_ACCEPTANCE, "OBJ-1", null);
        return SiadapInterimFeedback.create(evaluationId, null, null, null, null,
                List.of(), List.of(), List.of(pending));
    }

    @Test
    void negotiatesWithNullCommentWhenEmployeeAndPendingAcceptance() {
        SiadapEvaluation evaluation = buildEvaluation();
        UUID evalUuid = UUID.fromString(evaluation.getId().getStringValor());
        UUID revisionId = UUID.randomUUID();
        SiadapInterimFeedback feedback = buildFeedbackWithPendingRevision(evalUuid, revisionId);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluation.getEmployeeId()));
        when(feedbackRepository.findByEvaluationId(evalUuid)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any(SiadapInterimFeedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(SiadapInterimFeedback.class))).thenReturn(new SiadapInterimFeedbackDTO());

        NegotiateObjectiveRevisionCommand command = new NegotiateObjectiveRevisionCommand(
                evalUuid.toString(), revisionId.toString(), null);

        ResponseEntity<SiadapInterimFeedbackDTO> response = handler.handle(command);

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<SiadapInterimFeedback> captor = ArgumentCaptor.forClass(SiadapInterimFeedback.class);
        verify(feedbackRepository, times(1)).save(captor.capture());

        ObjectiveRevision saved = captor.getValue().getObjectiveRevisions().stream()
                .filter(r -> revisionId.equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(AcceptanceStatus.NEGOTIATING, saved.getApprovalStatus());
        assertNull(saved.getLastNegotiationComment());
    }

    @Test
    void negotiatesWithNonNullComment() {
        SiadapEvaluation evaluation = buildEvaluation();
        UUID evalUuid = UUID.fromString(evaluation.getId().getStringValor());
        UUID revisionId = UUID.randomUUID();
        SiadapInterimFeedback feedback = buildFeedbackWithPendingRevision(evalUuid, revisionId);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluation.getEmployeeId()));
        when(feedbackRepository.findByEvaluationId(evalUuid)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any(SiadapInterimFeedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(SiadapInterimFeedback.class))).thenReturn(new SiadapInterimFeedbackDTO());

        NegotiateObjectiveRevisionRequestDTO body = new NegotiateObjectiveRevisionRequestDTO("Preciso de mais detalhe");
        NegotiateObjectiveRevisionCommand command = new NegotiateObjectiveRevisionCommand(
                evalUuid.toString(), revisionId.toString(), body);

        handler.handle(command);

        ArgumentCaptor<SiadapInterimFeedback> captor = ArgumentCaptor.forClass(SiadapInterimFeedback.class);
        verify(feedbackRepository, times(1)).save(captor.capture());

        ObjectiveRevision saved = captor.getValue().getObjectiveRevisions().stream()
                .filter(r -> revisionId.equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("Preciso de mais detalhe", saved.getLastNegotiationComment());
    }

    @Test
    void throwsNotFoundWhenEvaluationDoesNotExist() {
        when(evaluationRepository.findById(any())).thenReturn(Optional.empty());

        NegotiateObjectiveRevisionCommand command = new NegotiateObjectiveRevisionCommand(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), null);

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(404, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void throwsForbiddenWhenCurrentUserIsNotTheEmployee() {
        SiadapEvaluation evaluation = buildEvaluation();

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.gerarNovo());

        NegotiateObjectiveRevisionCommand command = new NegotiateObjectiveRevisionCommand(
                evaluation.getId().getStringValor(), UUID.randomUUID().toString(), null);

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(403, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
    }
}
