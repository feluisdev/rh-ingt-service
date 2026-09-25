package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.application.service.SiadapObjectivesWindowPolicy;
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
class ProposeObjectiveRevisionCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapInterimFeedbackRepository feedbackRepository;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapObjectivesWindowPolicy windowPolicy;

    @Mock
    private SiadapInterimFeedbackMapper mapper;

    @Mock
    private CurrentEmployeeResolver currentEmployeeResolver;

    @InjectMocks
    private ProposeObjectiveRevisionCommandHandler handler;

    private SiadapEvaluation buildEvaluation() {
        return SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));
    }

    private SiadapInterimFeedback buildFeedbackWithDraftRevision(UUID evaluationId, UUID revisionId) {
        ObjectiveRevision draft = ObjectiveRevision.create(
                revisionId, "Objetivo atual", "Justificação", "Novo objetivo SMART",
                null, "OBJ-1", null);
        return SiadapInterimFeedback.create(evaluationId, null, null, null, null,
                List.of(), List.of(), List.of(draft));
    }

    @Test
    void proposesRevisionWhenEvaluatorAndActivePeriodExist() {
        SiadapEvaluation evaluation = buildEvaluation();
        UUID evalUuid = UUID.fromString(evaluation.getId().getStringValor());
        UUID revisionId = UUID.randomUUID();
        SiadapInterimFeedback feedback = buildFeedbackWithDraftRevision(evalUuid, revisionId);

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluation.getEvaluatorId()));
        when(feedbackRepository.findByEvaluationId(evalUuid)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any(SiadapInterimFeedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(SiadapInterimFeedback.class))).thenReturn(new SiadapInterimFeedbackDTO());

        ProposeObjectiveRevisionCommand command = new ProposeObjectiveRevisionCommand(
                evalUuid.toString(), revisionId.toString());

        ResponseEntity<SiadapInterimFeedbackDTO> response = handler.handle(command);

        assertEquals(200, response.getStatusCode().value());

        verify(windowPolicy, times(1)).requireRevisionOpenFor(YEAR);

        ArgumentCaptor<SiadapInterimFeedback> captor = ArgumentCaptor.forClass(SiadapInterimFeedback.class);
        verify(feedbackRepository, times(1)).save(captor.capture());

        SiadapInterimFeedback saved = captor.getValue();
        ObjectiveRevision savedRevision = saved.getObjectiveRevisions().stream()
                .filter(r -> revisionId.equals(r.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(AcceptanceStatus.PENDING_ACCEPTANCE, savedRevision.getApprovalStatus());
    }

    @Test
    void throwsNotFoundWhenEvaluationDoesNotExist() {
        when(evaluationRepository.findById(any())).thenReturn(Optional.empty());

        ProposeObjectiveRevisionCommand command = new ProposeObjectiveRevisionCommand(
                UUID.randomUUID().toString(), UUID.randomUUID().toString());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(404, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void throwsForbiddenWhenCurrentUserIsNotTheEvaluator() {
        SiadapEvaluation evaluation = buildEvaluation();

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.gerarNovo());

        ProposeObjectiveRevisionCommand command = new ProposeObjectiveRevisionCommand(
                evaluation.getId().getStringValor(), UUID.randomUUID().toString());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(403, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
        verify(windowPolicy, never()).requireRevisionOpenFor(anyInt());
    }

    @Test
    void throwsBadRequestWhenWindowPolicyRefusesTheEvaluationYear() {
        SiadapEvaluation evaluation = buildEvaluation();

        when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
        when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(evaluation.getEvaluatorId()));
        doThrow(IgrpResponseStatusException.badRequest("Prazo não configurado para este ano"))
                .when(windowPolicy).requireRevisionOpenFor(YEAR);

        ProposeObjectiveRevisionCommand command = new ProposeObjectiveRevisionCommand(
                evaluation.getId().getStringValor(), UUID.randomUUID().toString());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        verify(feedbackRepository, never()).save(any());
    }
}
