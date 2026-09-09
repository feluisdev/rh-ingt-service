package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.dto.AssignBudgetDTO;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
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

// A-135-2AA: prova que, com o corpo real da interface (sem activityId no JSON -- esse
// campo vem do {id} do caminho, tal como TaticalController.assignBudget o preenche
// antes de construir este comando), a atividade B chega a save() com o orçamento
// correto e transita PENDING_BUDGET -> DRAFT. Um 200 não prova que gravou (ver
// 135-EXECUCAO-P5-CICLO.md); o ArgumentCaptor sobre save() prova.
//
// Contraprova executada e revertida: repondo @NotNull em AssignBudgetDTO.activityId,
// o pedido real (sem activityId no corpo) nunca chega a este handler -- falha antes,
// na vinculação @Valid do controlador -- pelo que
// AssignBudgetDTOValidationTest#bodyWithoutActivityIdDoesNotViolateConstraint falha
// nesse cenário. Este teste, por si, exercita o handler admitindo que o controlador já
// fez a sua parte (copiar o id do caminho); a prova de que o corpo do PEDIDO em si não
// depende de enviar activityId vive no teste de validação, não aqui.
@ExtendWith(MockitoExtension.class)
class AssignTacticalActivityBudgetCommandHandlerTest {

  @Mock
  private EconomicClassifierPort economicClassifierPort;

  @Mock
  private TacticalActivityRepository activityRepository;

  @InjectMocks
  private AssignTacticalActivityBudgetCommandHandler handler;

  private TacticalActivity pendingBudgetActivity() {
    return TacticalActivity.create(
        UUID.randomUUID(),
        StrategicGoalId.from(UUID.randomUUID()),
        UUID.randomUUID(),
        "Otimizar processos internos da DGPOG",
        null,
        null,
        null,
        null,
        null,
        DateRange.of(LocalDate.now(), LocalDate.now().plusDays(30)),
        null, // sem orçamento -- nasce PENDING_BUDGET, como a Atividade B do 135-08
        null);
  }

  // Corpo tal como o controlador o entrega ao comando depois de
  // assignBudgetRequest.setActivityId(id) -- ou seja, já com o activityId do caminho,
  // não do JSON que o cliente enviou.
  private AssignBudgetDTO budgetRequestFor(UUID activityId) {
    AssignBudgetDTO dto = new AssignBudgetDTO();
    dto.setActivityId(activityId);
    dto.setBudgetEstimated(new BigDecimal("5000.00"));
    dto.setEconomicClassifier("02.03.01");
    return dto;
  }

  @Test
  void handleSavesTheBudgetAndTransitionsToDraft() {
    TacticalActivity activity = pendingBudgetActivity();
    AssignBudgetDTO request = budgetRequestFor(activity.getId().getValor().getValor());

    when(activityRepository.findById(any(TacticalActivityId.class)))
        .thenReturn(Optional.of(activity));
    when(economicClassifierPort.getBudget("02.03.01"))
        .thenReturn(new BudgetInfoDTO("02.03.01", new BigDecimal("100000.00"), "CVE", LocalDate.now()));
    when(activityRepository.save(any(TacticalActivity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<TacticalActivityResponseDTO> response =
        handler.handle(new AssignTacticalActivityBudgetCommand(request));

    ArgumentCaptor<TacticalActivity> captor = ArgumentCaptor.forClass(TacticalActivity.class);
    verify(activityRepository).save(captor.capture());

    TacticalActivity saved = captor.getValue();
    assertEquals(0, new BigDecimal("5000.00").compareTo(saved.getBudget().getEstimatedAmount()),
        "O objeto passado a save() -- não a resposta 200 -- é a prova de que o montante gravou");
    assertEquals("02.03.01", saved.getBudget().getClassifier().getCode());
    assertEquals("DRAFT", saved.getStatus().getCode(),
        "PENDING_BUDGET -> DRAFT é a transição que a Atividade B mediu no 135-08 depois da correção");

    assertEquals(200, response.getStatusCode().value());
    assertEquals("DRAFT", response.getBody().getStatus());
  }
}
