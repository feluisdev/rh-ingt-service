package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.ActivityWorkflowResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WorkflowCommentDTO;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

// Phase 134 / POR-02: proves the PAA activity window gate lives at the save() boundary, not only
// in the response status code -- see the ArgumentCaptor case for the paaLevel source, and the
// never().save(any()) case for the fact that a rejection never reaches the aggregate.
@ExtendWith(MockitoExtension.class)
class ApproveTacticalActivityCommandHandlerTest {

  @Mock
  private TacticalActivityRepository repository;

  @Mock
  private PaaActivityWindowPolicy windowPolicy;

  @InjectMocks
  private ApproveTacticalActivityCommandHandler handler;

  private TacticalActivity submittedActivity(PaaLevel paaLevel, UUID responsibleWho) {
    TacticalActivity draft = TacticalActivity.create(
        UUID.randomUUID(),
        StrategicGoalId.from(UUID.randomUUID()),
        UUID.randomUUID(),
        "Atividade submetida",
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

  // T-134: the window is closed -- handle() must throw, and, more importantly, the aggregate
  // must never reach save(). The status code alone does not prove this; only a save() that
  // never happens does.
  @Test
  void handleRejectsAndNeverSavesWhenWindowIsClosed() {
    TacticalActivity activity = submittedActivity(PaaLevel.UNIT_LEVEL, null);

    when(repository.findByIdFull(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    doThrow(IgrpResponseStatusException.badRequest(
            "Prazo não configurado para a submissão de atividades do PAA"))
        .when(windowPolicy)
        .requireOpenFor(any(PaaLevel.class));

    ApproveTacticalActivityCommand command = new ApproveTacticalActivityCommand();
    command.setId(activity.getId().getStringValor());

    assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
    verify(repository, never()).save(any());
  }

  // T-134: the level queried is the loaded entity's paaLevel, never a value the client could
  // supply -- INDIVIDUAL_LEVEL is deliberately not the create() default (UNIT_LEVEL), so the
  // captured value distinguishes the correct source from an accidental default.
  @Test
  void handleQueriesWindowForTheLoadedActivitysPaaLevel() {
    UUID responsibleWho = UUID.randomUUID();
    TacticalActivity activity = submittedActivity(PaaLevel.INDIVIDUAL_LEVEL, responsibleWho);

    when(repository.findByIdFull(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    when(repository.save(any(TacticalActivity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ApproveTacticalActivityCommand command = new ApproveTacticalActivityCommand();
    command.setId(activity.getId().getStringValor());

    handler.handle(command);

    ArgumentCaptor<PaaLevel> captor = ArgumentCaptor.forClass(PaaLevel.class);
    verify(windowPolicy).requireOpenFor(captor.capture());
    assertEquals(PaaLevel.INDIVIDUAL_LEVEL, captor.getValue());
    assertNotEquals(PaaLevel.UNIT_LEVEL, captor.getValue());
  }

  // With the window open, approve() still proceeds normally -- the gate must not break the
  // happy path it now guards, and the command's comment is preserved when present.
  @Test
  void handleApprovesAndSavesWhenWindowIsOpen() {
    TacticalActivity activity = submittedActivity(PaaLevel.UNIT_LEVEL, null);

    when(repository.findByIdFull(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    when(repository.save(any(TacticalActivity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ApproveTacticalActivityCommand command = new ApproveTacticalActivityCommand();
    command.setId(activity.getId().getStringValor());
    command.setWorkflowcomment(new WorkflowCommentDTO("Aprovado, prossiga"));

    ResponseEntity<ActivityWorkflowResponseDTO> response = handler.handle(command);

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    verify(repository, times(1)).save(any(TacticalActivity.class));

    ActivityWorkflowResponseDTO body = response.getBody();
    assertNotNull(body);
    assertNotEquals(body.getPreviousStatus(), body.getCurrentStatus());
    assertEquals("Aprovado, prossiga", body.getComment());
  }
}
