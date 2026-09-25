package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.ActivityWorkflowResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WorkflowCommentDTO;
import cv.igrp.RH_Service.sigdi.application.service.ActivityApprovalHistoryRecorder;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

// Phase 134 / POR-02: RejectTacticalActivityCommandHandler already refused first for a different
// reason -- a missing comment ("Comentário é obrigatório para rejeitar"). The window gate had to
// be placed ABOVE that validation, not below it: outside the window, the reason the user needs to
// hear is the deadline, and case 2 below is the one that proves the order was not inverted.
@ExtendWith(MockitoExtension.class)
class RejectTacticalActivityCommandHandlerTest {

  private static final String DEADLINE_MESSAGE = "Prazo não configurado para a submissão de atividades do PAA";
  private static final String COMMENT_MESSAGE = "Comentário é obrigatório para rejeitar";

  @Mock
  private TacticalActivityRepository repository;

  @Mock
  private PaaActivityWindowPolicy windowPolicy;

  @Mock
  private ActivityApprovalHistoryRecorder historyRecorder;

  @InjectMocks
  private RejectTacticalActivityCommandHandler handler;

  private TacticalActivity pendingActivity(PaaLevel paaLevel, UUID responsibleWho) {
    TacticalActivity draft = TacticalActivity.create(
        UUID.randomUUID(),
        StrategicGoalId.from(UUID.randomUUID()),
        UUID.randomUUID(),
        "Atividade pendente",
        null,
        null,
        null,
        responsibleWho,
        null,
        DateRange.of(LocalDate.now(), LocalDate.now().plusDays(10)),
        Budget.of(new BigDecimal("1000"), "02.02.01"),
        paaLevel);
    return draft.submit();
  }

  private RejectTacticalActivityCommand commandFor(TacticalActivity activity, String comment) {
    RejectTacticalActivityCommand command = new RejectTacticalActivityCommand();
    command.setId(activity.getId().getStringValor());
    if (comment != null) {
      command.setWorkflowcomment(new WorkflowCommentDTO(comment));
    }
    return command;
  }

  // Case 1: window closed, comment present and valid -- must still refuse on the window, and
  // never reach save().
  @Test
  void handleRejectsAndNeverSavesWhenWindowIsClosedWithValidComment() {
    TacticalActivity activity = pendingActivity(PaaLevel.UNIT_LEVEL, null);

    when(repository.findByIdFull(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    doThrow(IgrpResponseStatusException.badRequest(DEADLINE_MESSAGE))
        .when(windowPolicy)
        .requireOpenFor(any(PaaLevel.class));

    RejectTacticalActivityCommand command = commandFor(activity, "Comentário válido e completo");

    IgrpResponseStatusException thrown = org.junit.jupiter.api.Assertions.assertThrows(
        IgrpResponseStatusException.class, () -> handler.handle(command));
    assertEquals(DEADLINE_MESSAGE, thrown.getBody().getTitle());
    verify(repository, never()).save(any());
    // A-135-2AB (Phase 136, plano 136-10): uma transição recusada não deixa rasto nenhum.
    verify(historyRecorder, never()).record(any(), any(), any(), any(), any());
  }

  // Case 2: THE case that proves the order. Window closed AND comment blank at the same time --
  // the message that reaches the user must be the deadline one, not the comment one. If the gate
  // were placed after the comment validation, this test fails with COMMENT_MESSAGE instead.
  @Test
  void handleRejectsWithDeadlineMessageWhenWindowIsClosedAndCommentIsBlank() {
    TacticalActivity activity = pendingActivity(PaaLevel.UNIT_LEVEL, null);

    when(repository.findByIdFull(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    doThrow(IgrpResponseStatusException.badRequest(DEADLINE_MESSAGE))
        .when(windowPolicy)
        .requireOpenFor(any(PaaLevel.class));

    RejectTacticalActivityCommand command = commandFor(activity, "");

    IgrpResponseStatusException thrown = org.junit.jupiter.api.Assertions.assertThrows(
        IgrpResponseStatusException.class, () -> handler.handle(command));
    assertEquals(DEADLINE_MESSAGE, thrown.getBody().getTitle());
    assertNotEquals(COMMENT_MESSAGE, thrown.getBody().getTitle());
    verify(repository, never()).save(any());
    verify(historyRecorder, never()).record(any(), any(), any(), any(), any());
  }

  // Case 3: window open, comment blank -- the pre-existing comment validation must still fire.
  // The window gate did not swallow it.
  @Test
  void handleRejectsWithCommentMessageWhenWindowIsOpenAndCommentIsBlank() {
    TacticalActivity activity = pendingActivity(PaaLevel.UNIT_LEVEL, null);

    when(repository.findByIdFull(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));

    RejectTacticalActivityCommand command = commandFor(activity, "   ");

    IgrpResponseStatusException thrown = org.junit.jupiter.api.Assertions.assertThrows(
        IgrpResponseStatusException.class, () -> handler.handle(command));
    assertEquals(COMMENT_MESSAGE, thrown.getBody().getTitle());
    verify(repository, never()).save(any());
  }

  // Case 4: window open, comment valid -- saves once, DTO carries the comment and the status
  // transition.
  @Test
  void handleRejectsAndSavesWhenWindowIsOpenAndCommentIsValid() {
    TacticalActivity activity = pendingActivity(PaaLevel.UNIT_LEVEL, null);

    when(repository.findByIdFull(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    when(repository.save(any(TacticalActivity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RejectTacticalActivityCommand command = commandFor(activity, "Faltam evidências no dossier");

    ResponseEntity<ActivityWorkflowResponseDTO> response = handler.handle(command);

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    verify(repository, times(1)).save(any(TacticalActivity.class));

    ActivityWorkflowResponseDTO body = response.getBody();
    assertNotNull(body);
    assertNotEquals(body.getPreviousStatus(), body.getCurrentStatus());
    assertEquals("Faltam evidências no dossier", body.getComment());

    // A-135-2AB (Phase 136, plano 136-10): rejeitar deixa rasto, com o comentário do fluxo.
    verify(historyRecorder, times(1)).record(any(), eq("PENDING_TACTICAL"), eq("DRAFT"), eq("DRAFT"),
        eq("Faltam evidências no dossier"));
  }
}
