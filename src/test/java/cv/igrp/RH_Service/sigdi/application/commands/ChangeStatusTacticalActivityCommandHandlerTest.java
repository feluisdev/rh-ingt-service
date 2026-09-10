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
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.TaticalActivityStatusDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TaticalActivityHistoryEntityRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

// A-132-111 / COR-01 (Phase 136, D-47 reverts T-139's 2026-09-05 exclusion of this handler --
// the sixth of six TacticalActivity transition handlers A-132-111 named; the other five gained
// this same test shape in Phase 134, SubmitTacticalActivityCommandHandlerTest). This file was a
// TODO stub before this plan: the handler had zero tests, so nothing could fail when the gate
// was missing.
//
// D-54 (136-CONTEXT.md): reachable via PATCH /api/tactical/activities/[id]/status, but
// changeActivityStatus (functions/tactical.ts:406) has no caller in the interface today -- the
// live reproduction in 136-12 exercises the BFF route directly, not the UI.
@ExtendWith(MockitoExtension.class)
class ChangeStatusTacticalActivityCommandHandlerTest {

  @Mock
  private TacticalActivityRepository repository;

  @Mock
  private TaticalActivityHistoryEntityRepository historyRepository;

  @Mock
  private TacticalActivitiesEntityRepository entityRepository;

  @Mock
  private SecurityContextHelper securityContextHelper;

  @Mock
  private PaaActivityWindowPolicy windowPolicy;

  @InjectMocks
  private ChangeStatusTacticalActivityCommandHandler handler;

  private TacticalActivity pendingTacticalActivity(PaaLevel paaLevel) {
    TacticalActivity draft = TacticalActivity.create(
        UUID.randomUUID(),
        StrategicGoalId.from(UUID.randomUUID()),
        UUID.randomUUID(),
        "Atividade pendente de aprovação tática",
        null, null, null, null, null,
        DateRange.of(LocalDate.now(), LocalDate.now().plusDays(10)),
        Budget.of(new BigDecimal("1000"), "02.02.01"),
        paaLevel);
    return draft.submit();
  }

  private ChangeStatusTacticalActivityCommand rejectCommandFor(TacticalActivity activity) {
    TaticalActivityStatusDTO dto = new TaticalActivityStatusDTO();
    dto.setStatus("REJECTED");
    return new ChangeStatusTacticalActivityCommand(dto, activity.getId().getStringValor());
  }

  // T-136 contraprova: janela fechada -- handle() tem de recusar, e nem o repositório de
  // atividades nem o de histórico podem ser tocados. Escrever histórico de uma transição que
  // não aconteceu seria pior do que não recusar.
  @Test
  void handleRejectsAndWritesNothingWhenWindowIsClosed() {
    TacticalActivity activity = pendingTacticalActivity(PaaLevel.UNIT_LEVEL);

    when(repository.findByIdFull(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    doThrow(IgrpResponseStatusException.badRequest(
            "Prazo não configurado para a submissão de atividades do PAA"))
        .when(windowPolicy)
        .requireOpenFor(any(PaaLevel.class));

    ChangeStatusTacticalActivityCommand command = rejectCommandFor(activity);

    assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

    verify(repository, never()).save(any());
    verify(historyRepository, never()).save(any());
    verify(entityRepository, never()).findById(any());
  }

  // Com a janela aberta, a transição continua a funcionar e a escrever o histórico como antes.
  @Test
  void handleTransitionsAndSavesWhenWindowIsOpen() {
    TacticalActivity activity = pendingTacticalActivity(PaaLevel.UNIT_LEVEL);

    when(repository.findByIdFull(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    when(repository.save(any(TacticalActivity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(entityRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    ChangeStatusTacticalActivityCommand command = rejectCommandFor(activity);

    ResponseEntity<Map<String, ?>> response = handler.handle(command);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("DRAFT", response.getBody().get("status"));
    verify(repository, times(1)).save(any(TacticalActivity.class));
    verify(windowPolicy).requireOpenFor(PaaLevel.UNIT_LEVEL);
  }
}
