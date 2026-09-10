package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

// Phase 134 / POR-02: proves the PAA activity window gate lives at the save() boundary, not only
// in the response status code -- see the ArgumentCaptor case for the paaLevel source, and the
// never().save(any()) case for the fact that acceptance never reaches the aggregate.
@ExtendWith(MockitoExtension.class)
class AcceptTacticalActivityCommandHandlerTest {

  @Mock
  private TacticalActivityRepository repository;

  @Mock
  private PaaActivityWindowPolicy windowPolicy;

  @Mock
  private ActivityApprovalHistoryRecorder historyRecorder;

  @InjectMocks
  private AcceptTacticalActivityCommandHandler handler;

  // accept() only succeeds from PENDING_ACCEPTANCE or NEGOTIATING; create() only assigns an
  // initial acceptanceStatus (PENDING_ACCEPTANCE) when paaLevel is INDIVIDUAL_LEVEL, so that is
  // the only level for which a valid starting state exists here.
  private TacticalActivity pendingAcceptanceActivity(UUID responsibleWho) {
    return TacticalActivity.create(
        UUID.randomUUID(),
        StrategicGoalId.from(UUID.randomUUID()),
        UUID.randomUUID(),
        "Atividade individual pendente de aceitação",
        null,
        null,
        null,
        responsibleWho,
        null,
        DateRange.of(LocalDate.now(), LocalDate.now().plusDays(10)),
        Budget.of(new BigDecimal("1000"), "02.02.01"),
        PaaLevel.INDIVIDUAL_LEVEL);
  }

  // T-134-13: the window is closed -- handle() must throw, and, more importantly, the aggregate
  // must never reach save(). The status code alone does not prove this; only a save() that never
  // happens does.
  @Test
  void handleRejectsAndNeverSavesWhenWindowIsClosed() {
    TacticalActivity activity = pendingAcceptanceActivity(UUID.randomUUID());

    when(repository.findById(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    doThrow(IgrpResponseStatusException.badRequest(
            "Prazo não configurado para a submissão de atividades do PAA"))
        .when(windowPolicy)
        .requireOpenFor(any(PaaLevel.class));

    AcceptTacticalActivityCommand command = new AcceptTacticalActivityCommand(activity.getId().getStringValor());

    assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
    verify(repository, never()).save(any());
    // A-135-2AB (Phase 136, plano 136-10): uma transição recusada não deixa rasto nenhum.
    verify(historyRecorder, never()).record(any(), any(), any(), any(), any());
  }

  // T-134-14: the level queried is the loaded entity's paaLevel, never a value the client could
  // supply. INDIVIDUAL_LEVEL is not create()'s default (UNIT_LEVEL) -- it is also the only level
  // for which accept() has a valid starting state, so the captured value still distinguishes the
  // correct source from an accidental default.
  @Test
  void handleQueriesWindowForTheLoadedActivitysPaaLevel() {
    UUID responsibleWho = UUID.randomUUID();
    TacticalActivity activity = pendingAcceptanceActivity(responsibleWho);

    when(repository.findById(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    when(repository.save(any(TacticalActivity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AcceptTacticalActivityCommand command = new AcceptTacticalActivityCommand(activity.getId().getStringValor());

    handler.handle(command);

    ArgumentCaptor<PaaLevel> captor = ArgumentCaptor.forClass(PaaLevel.class);
    verify(windowPolicy).requireOpenFor(captor.capture());
    assertEquals(PaaLevel.INDIVIDUAL_LEVEL, captor.getValue());
    assertNotEquals(PaaLevel.UNIT_LEVEL, captor.getValue());
  }

  // With the window open, accept() still proceeds normally -- the gate must not break the happy
  // path it now guards. acceptanceStatus is non-null after a real accept() transition, so this
  // exercises the TRUE branch of the handler's `if (saved.getAcceptanceStatus() != null)` guard;
  // the FALSE branch (null acceptanceStatus) is exercised in NegotiateTacticalActivityCommandHandlerTest.
  @Test
  void handleAcceptsAndSavesWhenWindowIsOpen() {
    TacticalActivity activity = pendingAcceptanceActivity(UUID.randomUUID());

    when(repository.findById(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    when(repository.save(any(TacticalActivity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AcceptTacticalActivityCommand command = new AcceptTacticalActivityCommand(activity.getId().getStringValor());

    ResponseEntity<TacticalActivityResponseDTO> response = handler.handle(command);

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    verify(repository, times(1)).save(any(TacticalActivity.class));

    TacticalActivityResponseDTO body = response.getBody();
    assertNotNull(body);
    assertEquals(activity.getStatus().getCode(), body.getStatus());
    assertEquals(PaaLevel.INDIVIDUAL_LEVEL.getCode(), body.getPaaLevel());
    assertEquals(AcceptanceStatus.ACCEPTED.getCode(), body.getAcceptanceStatus());

    // A-135-2AB (Phase 136, plano 136-10): aceitar transiciona acceptanceStatus (não status) --
    // fromStatus vem de PENDING_ACCEPTANCE, toStatus/action de ACCEPTED.
    verify(historyRecorder, times(1)).record(any(), eq(AcceptanceStatus.PENDING_ACCEPTANCE.getCode()),
        eq(AcceptanceStatus.ACCEPTED.getCode()), eq(AcceptanceStatus.ACCEPTED.getCode()), any());
  }
}
