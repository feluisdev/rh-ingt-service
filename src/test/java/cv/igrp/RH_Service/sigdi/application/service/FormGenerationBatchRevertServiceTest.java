package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationRevertSkipReason;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyItem;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova da fronteira do apagável (Fase 120, plano 02, {@code PRZ-04}): o que
 * {@link FormGenerationBatchRevertService#revert(FormGenerationBatch)} apaga, o que mantém e
 * porquê. Cada teste tem nome que diz qual limite prova -- T-120-05 e T-120-06 do threat model
 * do plano.
 */
@ExtendWith(MockitoExtension.class)
class FormGenerationBatchRevertServiceTest {

  @Mock
  private SiadapEvaluationRepository evaluationRepository;

  private FormGenerationBatchRevertService service;

  private final LocalDateTime now = LocalDateTime.of(2026, 8, 28, 10, 0);

  private void init() {
    service = new FormGenerationBatchRevertService(evaluationRepository);
  }

  private FormGenerationBatch batchWith(FormGenerationBatchItem... items) {
    FormGenerationBatch batch = mock(FormGenerationBatch.class);
    when(batch.getItems()).thenReturn(List.of(items));
    return batch;
  }

  private FormGenerationBatchItem createdItem(UUID formId) {
    return FormGenerationBatchItem.of(UUID.randomUUID(), "Colaborador", UUID.randomUUID(), "Unidade",
        FormGenerationOutcome.CREATED, formId, UUID.randomUUID(), null, null, now);
  }

  private SiadapEvaluation evaluationWith(UUID formId, EvaluationPhase phase, AcceptanceStatus acceptanceStatus,
      List<IndividualObjective> objectives, List<CompetencyItem> competencies) {
    // lenient(): o serviço sai cedo (short-circuit) quando a fase já não é OPEN, e por isso nem
    // sempre chama getAcceptanceStatus/getObjectives/getCompetencies -- são exactamente os
    // testes de fronteira de fase (2 e 3) que tornam esses stubs "desnecessários" aos olhos da
    // verificação estrita do Mockito, sem que isso seja um erro de teste.
    SiadapEvaluation evaluation = mock(SiadapEvaluation.class);
    Mockito.lenient().when(evaluation.getId()).thenReturn(SiadapEvaluationId.from(formId));
    Mockito.lenient().when(evaluation.getPhase()).thenReturn(phase);
    Mockito.lenient().when(evaluation.getAcceptanceStatus()).thenReturn(acceptanceStatus);
    Mockito.lenient().when(evaluation.getObjectives()).thenReturn(objectives);
    Mockito.lenient().when(evaluation.getCompetencies()).thenReturn(competencies);
    return evaluation;
  }

  // ------------------------------------------------------------------
  // 1. A avaliação exactamente como nasceu -- apagada.
  // ------------------------------------------------------------------
  @Test
  void revertDeletesCreatedItemBornExactlyAsItWasBorn() {
    init();
    UUID formId = UUID.randomUUID();
    FormGenerationBatchItem item = createdItem(formId);
    SiadapEvaluation evaluation = evaluationWith(formId, EvaluationPhase.OPEN, null, List.of(), List.of());
    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
    when(evaluationRepository.deleteById(any())).thenReturn(true);

    FormGenerationBatchRevertService.RevertOutcome outcome = service.revert(batchWith(item));

    assertEquals(List.of(formId), outcome.revertedFormIds());
    assertTrue(outcome.blockedFormIds().isEmpty());
    verify(evaluationRepository).deleteById(evaluation.getId());
  }

  // ------------------------------------------------------------------
  // 2. Limite de fase, lado de dentro: IN_PROGRESS -- a primeira fase que já não é apagável.
  // ------------------------------------------------------------------
  @Test
  void revertKeepsCreatedItemAtInProgress_PhaseAdvanced() {
    init();
    UUID formId = UUID.randomUUID();
    FormGenerationBatchItem item = createdItem(formId);
    SiadapEvaluation evaluation = evaluationWith(formId, EvaluationPhase.IN_PROGRESS, null, List.of(), List.of());
    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));

    FormGenerationBatchRevertService.RevertOutcome outcome = service.revert(batchWith(item));

    assertTrue(outcome.revertedFormIds().isEmpty());
    assertEquals(FormGenerationRevertSkipReason.PHASE_ADVANCED, outcome.blockedFormIds().get(formId));
    verify(evaluationRepository, never()).deleteById(any());
  }

  // ------------------------------------------------------------------
  // 3. Fases posteriores a IN_PROGRESS -- confirmação de que o motivo se mantém, não só no
  //    valor imediatamente a seguir a OPEN.
  // ------------------------------------------------------------------
  @Test
  void revertKeepsCreatedItemInLaterPhases_PhaseAdvanced() {
    for (EvaluationPhase phase : List.of(EvaluationPhase.SELF_EVALUATION, EvaluationPhase.MANAGER_EVALUATION,
        EvaluationPhase.HARMONIZATION, EvaluationPhase.CLOSED)) {
      evaluationRepository = mock(SiadapEvaluationRepository.class);
      init();
      UUID formId = UUID.randomUUID();
      FormGenerationBatchItem item = createdItem(formId);
      SiadapEvaluation evaluation = evaluationWith(formId, phase, null, List.of(), List.of());
      when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));

      FormGenerationBatchRevertService.RevertOutcome outcome = service.revert(batchWith(item));

      assertTrue(outcome.revertedFormIds().isEmpty(), "fase " + phase + " não devia apagar");
      assertEquals(FormGenerationRevertSkipReason.PHASE_ADVANCED, outcome.blockedFormIds().get(formId),
          "fase " + phase + " devia dar PHASE_ADVANCED");
    }
  }

  // ------------------------------------------------------------------
  // 4. Limite dentro de OPEN, lado de dentro: acceptanceStatus = PENDING_ACCEPTANCE.
  // ------------------------------------------------------------------
  @Test
  void revertKeepsCreatedItemAtOpenWithPendingAcceptance_ObjectivesAlreadyProposed() {
    init();
    UUID formId = UUID.randomUUID();
    FormGenerationBatchItem item = createdItem(formId);
    SiadapEvaluation evaluation = evaluationWith(formId, EvaluationPhase.OPEN,
        AcceptanceStatus.PENDING_ACCEPTANCE, List.of(), List.of());
    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));

    FormGenerationBatchRevertService.RevertOutcome outcome = service.revert(batchWith(item));

    assertTrue(outcome.revertedFormIds().isEmpty());
    assertEquals(FormGenerationRevertSkipReason.OBJECTIVES_ALREADY_PROPOSED, outcome.blockedFormIds().get(formId));
    verify(evaluationRepository, never()).deleteById(any());
  }

  // ------------------------------------------------------------------
  // 5. OPEN, acceptanceStatus nulo, mas com objetivo -- ainda apagável só pelo par
  //    fase/aceitação seria um erro; o teste prova que os objetivos também contam.
  // ------------------------------------------------------------------
  @Test
  void revertKeepsCreatedItemAtOpenWithExistingObjective_ObjectivesAlreadyProposed() {
    init();
    UUID formId = UUID.randomUUID();
    FormGenerationBatchItem item = createdItem(formId);
    IndividualObjective objective = mock(IndividualObjective.class);
    SiadapEvaluation evaluation = evaluationWith(formId, EvaluationPhase.OPEN, null, List.of(objective), List.of());
    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));

    FormGenerationBatchRevertService.RevertOutcome outcome = service.revert(batchWith(item));

    assertTrue(outcome.revertedFormIds().isEmpty());
    assertEquals(FormGenerationRevertSkipReason.OBJECTIVES_ALREADY_PROPOSED, outcome.blockedFormIds().get(formId));
    verify(evaluationRepository, never()).deleteById(any());
  }

  // ------------------------------------------------------------------
  // 6. OPEN, acceptanceStatus nulo, mas com competência.
  // ------------------------------------------------------------------
  @Test
  void revertKeepsCreatedItemAtOpenWithExistingCompetency_ObjectivesAlreadyProposed() {
    init();
    UUID formId = UUID.randomUUID();
    FormGenerationBatchItem item = createdItem(formId);
    CompetencyItem competency = mock(CompetencyItem.class);
    SiadapEvaluation evaluation = evaluationWith(formId, EvaluationPhase.OPEN, null, List.of(), List.of(competency));
    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));

    FormGenerationBatchRevertService.RevertOutcome outcome = service.revert(batchWith(item));

    assertTrue(outcome.revertedFormIds().isEmpty());
    assertEquals(FormGenerationRevertSkipReason.OBJECTIVES_ALREADY_PROPOSED, outcome.blockedFormIds().get(formId));
    verify(evaluationRepository, never()).deleteById(any());
  }

  // ------------------------------------------------------------------
  // 7. A avaliação já não existe -- EVALUATION_NOT_FOUND, sem excepção.
  // ------------------------------------------------------------------
  @Test
  void revertKeepsCreatedItemWhenEvaluationNotFound_EvaluationNotFound() {
    init();
    UUID formId = UUID.randomUUID();
    FormGenerationBatchItem item = createdItem(formId);
    when(evaluationRepository.findById(any())).thenReturn(Optional.empty());

    FormGenerationBatchRevertService.RevertOutcome outcome = service.revert(batchWith(item));

    assertTrue(outcome.revertedFormIds().isEmpty());
    assertEquals(FormGenerationRevertSkipReason.EVALUATION_NOT_FOUND, outcome.blockedFormIds().get(formId));
    verify(evaluationRepository, never()).deleteById(any());
  }

  // ------------------------------------------------------------------
  // 8. Correr a reversão duas vezes sobre o mesmo lote não rebenta -- a segunda passagem
  //    encontra a avaliação já apagada e regista EVALUATION_NOT_FOUND outra vez, sem excepção.
  // ------------------------------------------------------------------
  @Test
  void revertingTwiceAfterEvaluationNotFoundDoesNotThrow() {
    init();
    UUID formId = UUID.randomUUID();
    FormGenerationBatchItem item = createdItem(formId);
    when(evaluationRepository.findById(any())).thenReturn(Optional.empty());
    FormGenerationBatch batch = batchWith(item);

    FormGenerationBatchRevertService.RevertOutcome first = assertDoesNotThrow(() -> service.revert(batch));
    FormGenerationBatchRevertService.RevertOutcome second = assertDoesNotThrow(() -> service.revert(batch));

    assertEquals(FormGenerationRevertSkipReason.EVALUATION_NOT_FOUND, first.blockedFormIds().get(formId));
    assertEquals(FormGenerationRevertSkipReason.EVALUATION_NOT_FOUND, second.blockedFormIds().get(formId));
  }

  // ------------------------------------------------------------------
  // 9. ALREADY_EXISTED nunca é tocado -- é o pior modo de falha desta fase (T-120-05):
  //    generatedFormId aponta para a avaliação PRÉ-EXISTENTE que fez o lote saltar a criação,
  //    nunca para uma avaliação que este lote criou.
  // ------------------------------------------------------------------
  @Test
  void revertNeverTouchesAlreadyExistedItemEvenWithGeneratedFormId() {
    init();
    UUID preExistingFormId = UUID.randomUUID();
    FormGenerationBatchItem item = FormGenerationBatchItem.of(UUID.randomUUID(), "Colaborador",
        UUID.randomUUID(), "Unidade", FormGenerationOutcome.ALREADY_EXISTED, preExistingFormId,
        UUID.randomUUID(), null, null, now);

    FormGenerationBatchRevertService.RevertOutcome outcome = service.revert(batchWith(item));

    assertTrue(outcome.revertedFormIds().isEmpty());
    assertTrue(outcome.blockedFormIds().isEmpty());
    verify(evaluationRepository, never()).findById(any());
    verify(evaluationRepository, never()).deleteById(any());
  }

  // ------------------------------------------------------------------
  // 10. WOULD_CREATE, FAILED, SKIPPED e PENDING nunca são tocados.
  // ------------------------------------------------------------------
  @Test
  void revertNeverTouchesNonCreatedOutcomes() {
    init();
    FormGenerationBatchItem wouldCreate = FormGenerationBatchItem.of(UUID.randomUUID(), "A", UUID.randomUUID(), "U",
        FormGenerationOutcome.WOULD_CREATE, null, UUID.randomUUID(), null, null, now);
    FormGenerationBatchItem failed = FormGenerationBatchItem.of(UUID.randomUUID(), "B", UUID.randomUUID(), "U",
        FormGenerationOutcome.FAILED, null, null, null, "erro qualquer", now);
    FormGenerationBatchItem skipped = FormGenerationBatchItem.of(UUID.randomUUID(), "C", UUID.randomUUID(), "U",
        FormGenerationOutcome.SKIPPED, null, null, "MOTIVO", null, now);
    FormGenerationBatchItem pending = FormGenerationBatchItem.of(UUID.randomUUID(), "D", UUID.randomUUID(), "U",
        FormGenerationOutcome.PENDING, null, null, null, null, now);

    FormGenerationBatchRevertService.RevertOutcome outcome =
        service.revert(batchWith(wouldCreate, failed, skipped, pending));

    assertTrue(outcome.revertedFormIds().isEmpty());
    assertTrue(outcome.blockedFormIds().isEmpty());
    verify(evaluationRepository, never()).findById(any());
    verify(evaluationRepository, never()).deleteById(any());
  }

  // ------------------------------------------------------------------
  // 11. A falha a apagar um item não arrasta os restantes -- molde do D-12 do 119-03.
  // ------------------------------------------------------------------
  @Test
  void revertContinuesAfterFailureDeletingOneItem_doesNotCascadeToOthers() {
    init();
    UUID failingFormId = UUID.randomUUID();
    UUID okFormId = UUID.randomUUID();
    FormGenerationBatchItem failingItem = createdItem(failingFormId);
    FormGenerationBatchItem okItem = createdItem(okFormId);

    SiadapEvaluation failingEvaluation =
        evaluationWith(failingFormId, EvaluationPhase.OPEN, null, List.of(), List.of());
    SiadapEvaluation okEvaluation = evaluationWith(okFormId, EvaluationPhase.OPEN, null, List.of(), List.of());

    when(evaluationRepository.findById(SiadapEvaluationId.from(failingFormId)))
        .thenReturn(Optional.of(failingEvaluation));
    when(evaluationRepository.findById(SiadapEvaluationId.from(okFormId))).thenReturn(Optional.of(okEvaluation));
    when(evaluationRepository.deleteById(failingEvaluation.getId()))
        .thenThrow(new RuntimeException("falha de infraestrutura simulada"));
    when(evaluationRepository.deleteById(okEvaluation.getId())).thenReturn(true);

    FormGenerationBatchRevertService.RevertOutcome outcome =
        assertDoesNotThrow(() -> service.revert(batchWith(failingItem, okItem)));

    assertEquals(List.of(okFormId), outcome.revertedFormIds());
    assertTrue(outcome.blockedFormIds().containsKey(failingFormId));
    assertFalse(outcome.blockedFormIds().containsKey(okFormId));
  }
}
