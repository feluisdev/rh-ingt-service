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
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestResponseDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.ChangeRequestRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;

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

// A-132-108 / COR-01 (Phase 136, D-47 reverts T-139's 2026-09-05 exclusion of this handler --
// the handler had no dedicated test file before this plan). Proves the gate lives at the
// save() boundary, not only in the response status: the window-closed case verifies
// changeRequestRepository never saves, and the paaLevel captured is the loaded activity's, never
// the request's (D-27, Phase 134).
@ExtendWith(MockitoExtension.class)
class CreateChangeRequestCommandHandlerTest {

  private static final LocalDate START = LocalDate.of(2026, 1, 1);
  private static final LocalDate END = LocalDate.of(2026, 12, 31);
  private static final Budget BUDGET = Budget.of(new BigDecimal("1000"), "02.02.01");

  @Mock
  private TacticalActivityRepository activityRepository;

  @Mock
  private ChangeRequestRepository changeRequestRepository;

  @Mock
  private PaaActivityWindowPolicy windowPolicy;

  @InjectMocks
  private CreateChangeRequestCommandHandler handler;

  private TacticalActivity approvedActivity(PaaLevel paaLevel) {
    // responsibleWho non-null unconditionally: TacticalActivity.create() requires it for
    // INDIVIDUAL_LEVEL and ignores it otherwise, so one value covers both levels this test uses.
    TacticalActivity draft = TacticalActivity.create(
        UUID.randomUUID(), StrategicGoalId.from(UUID.randomUUID()), UUID.randomUUID(),
        "Atividade aprovada", "Descrição", "Justificação", "Localização", UUID.randomUUID(),
        "Metodologia", DateRange.of(START, END), BUDGET, paaLevel);
    return draft.submit().approve().approve();
  }

  private CreateChangeRequestCommand commandFor(TacticalActivity activity) {
    ChangeRequestDTO dto = new ChangeRequestDTO();
    dto.setFieldName("budget");
    dto.setCurrentValue("1000");
    dto.setProposedValue("2500");
    dto.setJustification(
        "Justificação com mais de cinquenta carateres para satisfazer a regra do ecrã.");
    return new CreateChangeRequestCommand(dto, activity.getId().getStringValor());
  }

  // T-136 contraprova: janela fechada -- recusa, e o pedido nunca é gravado.
  @Test
  void handleRejectsAndNeverSavesWhenWindowIsClosed() {
    TacticalActivity activity = approvedActivity(PaaLevel.UNIT_LEVEL);

    when(activityRepository.findById(any())).thenReturn(Optional.of(activity));
    doThrow(IgrpResponseStatusException.badRequest(
            "Prazo não configurado para a submissão de atividades do PAA"))
        .when(windowPolicy)
        .requireOpenFor(any(PaaLevel.class));

    CreateChangeRequestCommand command = commandFor(activity);

    assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
    verify(changeRequestRepository, never()).save(any());
  }

  // O nível consultado é o da atividade carregada, nunca um valor vindo do pedido -- o comando
  // não tem sequer um campo paaLevel.
  @Test
  void handleQueriesWindowForTheLoadedActivitysPaaLevel() {
    TacticalActivity activity = approvedActivity(PaaLevel.INDIVIDUAL_LEVEL);

    when(activityRepository.findById(any())).thenReturn(Optional.of(activity));
    when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    CreateChangeRequestCommand command = commandFor(activity);

    handler.handle(command);

    ArgumentCaptor<PaaLevel> captor = ArgumentCaptor.forClass(PaaLevel.class);
    verify(windowPolicy).requireOpenFor(captor.capture());
    assertEquals(PaaLevel.INDIVIDUAL_LEVEL, captor.getValue());
  }

  // Com a janela aberta, criar o pedido continua a funcionar como antes.
  @Test
  void handleCreatesAndSavesWhenWindowIsOpen() {
    TacticalActivity activity = approvedActivity(PaaLevel.UNIT_LEVEL);

    when(activityRepository.findById(any())).thenReturn(Optional.of(activity));
    when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    CreateChangeRequestCommand command = commandFor(activity);

    ResponseEntity<ChangeRequestResponseDTO> response = handler.handle(command);

    assertEquals(201, response.getStatusCode().value());
    verify(changeRequestRepository, times(1)).save(any());
  }
}
