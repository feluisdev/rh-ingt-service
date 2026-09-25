package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestStatus;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WorkflowCommentDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.ChangeRequest;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.ChangeRequestRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.ChangeRequestId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;

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

/**
 * A-132-109 / COR-01 (Phase 136, D-47 reverts T-139's 2026-09-05 exclusion of this handler).
 * Before this plan the handler had no tests and did not load the activity at all -- these prove
 * both the new load (and its not-found wording, shared with ApproveChangeRequestCommandHandler)
 * and the gate that depends on it.
 */
@ExtendWith(MockitoExtension.class)
class RejectChangeRequestCommandHandlerTest {

  private static final UUID INSTITUTION_ID = UUID.randomUUID();
  private static final StrategicGoalId STRATEGIC_GOAL_ID = StrategicGoalId.from(UUID.randomUUID());
  private static final LocalDate START = LocalDate.of(2026, 1, 1);
  private static final LocalDate END = LocalDate.of(2026, 12, 31);
  private static final Budget BUDGET = Budget.of(new BigDecimal("1000"), "02.02.01");

  @Mock
  private ChangeRequestRepository changeRequestRepository;

  @Mock
  private TacticalActivityRepository activityRepository;

  @Mock
  private PaaActivityWindowPolicy windowPolicy;

  @InjectMocks
  private RejectChangeRequestCommandHandler handler;

  private TacticalActivity approvedActivity(PaaLevel paaLevel) {
    // responsibleWho non-null unconditionally: TacticalActivity.create() requires it for
    // INDIVIDUAL_LEVEL and ignores it otherwise, so one value covers both levels this test uses.
    TacticalActivity draft = TacticalActivity.create(
        INSTITUTION_ID, STRATEGIC_GOAL_ID, UUID.randomUUID(),
        "Atividade aprovada", "Descrição", "Justificação", "Localização", UUID.randomUUID(),
        "Metodologia", DateRange.of(START, END), BUDGET, paaLevel);
    return draft.submit().approve().approve();
  }

  private ChangeRequest pendingRequest(TacticalActivity activity) {
    return ChangeRequest.reconstruct(ChangeRequestId.gerarNovo(), INSTITUTION_ID, activity.getId(),
        "budget", "1000", "2500",
        "Justificação com mais de cinquenta carateres para satisfazer a regra do ecrã.",
        ChangeRequestStatus.PENDING, null, null);
  }

  private RejectChangeRequestCommand commandFor(ChangeRequest cr) {
    RejectChangeRequestCommand command = new RejectChangeRequestCommand();
    command.setId(cr.getId().getValor().getValor().toString());
    WorkflowCommentDTO comment = new WorkflowCommentDTO();
    comment.setComment("Justificação da recusa, com mais de dez carateres.");
    command.setWorkflowcomment(comment);
    return command;
  }

  // T-136 contraprova: janela fechada -- recusa, e o pedido nunca é gravado.
  @Test
  void handleRejectsAndNeverSavesWhenWindowIsClosed() {
    TacticalActivity activity = approvedActivity(PaaLevel.UNIT_LEVEL);
    ChangeRequest cr = pendingRequest(activity);

    when(changeRequestRepository.findById(any())).thenReturn(Optional.of(cr));
    when(activityRepository.findById(any())).thenReturn(Optional.of(activity));
    doThrow(IgrpResponseStatusException.badRequest(
            "Prazo não configurado para a submissão de atividades do PAA"))
        .when(windowPolicy)
        .requireOpenFor(any(PaaLevel.class));

    RejectChangeRequestCommand command = commandFor(cr);

    assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
    verify(changeRequestRepository, never()).save(any());
  }

  // Com a janela aberta, a recusa do pedido continua a funcionar como antes -- e o nível
  // consultado é o da atividade carregada, não um valor vindo do pedido.
  @Test
  void handleRejectsAndSavesWhenWindowIsOpen() {
    TacticalActivity activity = approvedActivity(PaaLevel.INDIVIDUAL_LEVEL);
    ChangeRequest cr = pendingRequest(activity);

    when(changeRequestRepository.findById(any())).thenReturn(Optional.of(cr));
    when(activityRepository.findById(any())).thenReturn(Optional.of(activity));
    when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    RejectChangeRequestCommand command = commandFor(cr);

    ResponseEntity<ChangeRequestResponseDTO> response = handler.handle(command);

    assertEquals(ChangeRequestStatus.REJECTED.getCode(), response.getBody().getStatus());
    verify(changeRequestRepository, times(1)).save(any());
    verify(windowPolicy).requireOpenFor(PaaLevel.INDIVIDUAL_LEVEL);
  }

  // A-132-109: o handler passa a carregar a atividade; se ela não existir, a recusa usa a mesma
  // mensagem que o ApproveChangeRequestCommandHandler já usa para o mesmo facto -- não duas
  // frases para um único facto.
  @Test
  void handleRefusesWithSameNotFoundMessageWhenActivityIsMissing() {
    TacticalActivity activity = approvedActivity(PaaLevel.UNIT_LEVEL);
    ChangeRequest cr = pendingRequest(activity);

    when(changeRequestRepository.findById(any())).thenReturn(Optional.of(cr));
    when(activityRepository.findById(any())).thenReturn(Optional.empty());

    RejectChangeRequestCommand command = commandFor(cr);

    IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
        () -> handler.handle(command));

    assertEquals("A atividade tática visada pelo pedido de alteração não foi encontrada",
        ex.getBody().getTitle());
    verify(changeRequestRepository, never()).save(any());
  }
}
