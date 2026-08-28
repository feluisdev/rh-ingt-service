package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationRevertSkipReason;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Decide, item a item de um {@link FormGenerationBatch}, o que se apaga e o que se mantém ao
 * desfazer um lote de geração de formulários (Fase 120, plano 02, {@code PRZ-04}).
 *
 * <p><b>A armadilha que este serviço existe para não cair.</b> O candidato a apagar é
 * <strong>sempre</strong> {@code outcome == FormGenerationOutcome.CREATED} e nunca "tem {@code
 * generatedFormId}" -- {@code SiadapFormGenerator.processResponsible} grava {@code
 * generatedFormId} também nos itens {@code ALREADY_EXISTED}, mas aí aponta para a avaliação
 * <strong>pré-existente</strong> que fez o lote saltar a criação, não para uma avaliação que este
 * lote criou. Apagar por presença do identificador em vez de por {@code outcome} destruiria
 * avaliações que este lote nunca criou, possivelmente com anos de história -- é o pior modo de
 * falha desta fase inteira (T-120-05 do threat model do plano), e é por isso que o filtro abaixo
 * compara sempre o {@code outcome}, nunca a nulidade de {@code generatedFormId} sozinha.
 *
 * <p><b>A fronteira dupla do apagável, com a razão medida.</b> Uma avaliação é apagável se e só
 * se estiver exactamente como nasceu: {@code phase == OPEN} <strong>e</strong> {@code
 * acceptanceStatus == null} <strong>e</strong> sem objetivos <strong>e</strong> sem competências.
 * Uma fronteira só por fase não chegaria: {@code SiadapEvaluation.contractualizeObjectives()} e
 * {@code negotiateObjectives()} mudam {@code acceptanceStatus} para {@code PENDING_ACCEPTANCE} ou
 * {@code NEGOTIATING} <strong>sem sair de {@code OPEN}</strong> -- há trabalho real de
 * contratualização feito dentro de {@code OPEN}, entre avaliador e avaliado, e apagar só por a
 * fase ainda ser {@code OPEN} destruiria esse trabalho (T-120-06 do threat model do plano). Os
 * dois limites -- {@code OPEN}/{@code IN_PROGRESS} e {@code acceptanceStatus} nulo/{@code
 * PENDING_ACCEPTANCE} -- estão provados por teste no valor exacto de cada lado, em {@code
 * FormGenerationBatchRevertServiceTest}.
 *
 * <p><b>Falha por item, nunca por lote.</b> Sem {@code @Transactional} sobre o varrimento, pela
 * mesma razão que {@code PeriodFormGenerationService} já documenta: uma transação única a
 * envolver tudo faria a falha de um item arrastar os restantes -- exatamente o que a decisão 2 do
 * operador (2026-08-28) proíbe. Cada falha é apanhada individualmente, registada com {@code
 * LOGGER.error}, e o item fica na lista de mantidos; o varrimento continua. Molde do {@code D-12}
 * do {@code 119-03} e do {@code PaaSubmissionPeriodExpiryScheduler} (Fase 117).
 *
 * <p><b>O que este serviço não faz.</b> Não marca o lote como desfeito nem chama {@code
 * FormGenerationBatchRepository} -- devolve só o resultado do varrimento. Quem persiste a
 * reversão no lote (via {@code FormGenerationBatch#markReverted}, Fase 120 plano 01) é o handler
 * do comando, no plano 120-03. Separação deliberada: a fronteira do apagável é testável sem tocar
 * no rasto do lote.
 */
@Component
public class FormGenerationBatchRevertService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FormGenerationBatchRevertService.class);

  private final SiadapEvaluationRepository evaluationRepository;

  public FormGenerationBatchRevertService(SiadapEvaluationRepository evaluationRepository) {
    this.evaluationRepository = evaluationRepository;
  }

  /**
   * Varre {@code batch.getItems()} e devolve, para cada item candidato, se foi apagado ou
   * mantido -- e porquê. Só é candidato o item com {@code outcome == CREATED} e {@code
   * generatedFormId != null}; qualquer outro {@code outcome} (incluindo {@code ALREADY_EXISTED})
   * nunca é examinado nem contado.
   */
  public RevertOutcome revert(FormGenerationBatch batch) {
    List<UUID> reverted = new ArrayList<>();
    Map<UUID, FormGenerationRevertSkipReason> blocked = new LinkedHashMap<>();

    for (FormGenerationBatchItem item : batch.getItems()) {
      if (item.getOutcome() != FormGenerationOutcome.CREATED || item.getGeneratedFormId() == null) {
        continue;
      }
      UUID formId = item.getGeneratedFormId();
      try {
        revertOne(formId, reverted, blocked);
      } catch (Exception e) {
        // Falha por item, nunca por lote (D-12 do 119-03): a exceção fica só neste item, e o
        // varrimento continua para os restantes.
        LOGGER.error("Falha ao desfazer a avaliação gerada com generatedFormId {}", formId, e);
        blocked.put(formId, FormGenerationRevertSkipReason.EVALUATION_NOT_FOUND);
      }
    }

    return new RevertOutcome(reverted, blocked);
  }

  private void revertOne(UUID formId, List<UUID> reverted, Map<UUID, FormGenerationRevertSkipReason> blocked) {
    Optional<SiadapEvaluation> found = evaluationRepository.findById(SiadapEvaluationId.from(formId));
    if (found.isEmpty()) {
      blocked.put(formId, FormGenerationRevertSkipReason.EVALUATION_NOT_FOUND);
      return;
    }

    SiadapEvaluation evaluation = found.get();
    if (evaluation.getPhase() != EvaluationPhase.OPEN) {
      blocked.put(formId, FormGenerationRevertSkipReason.PHASE_ADVANCED);
      return;
    }
    if (evaluation.getAcceptanceStatus() != null
        || !evaluation.getObjectives().isEmpty()
        || !evaluation.getCompetencies().isEmpty()) {
      blocked.put(formId, FormGenerationRevertSkipReason.OBJECTIVES_ALREADY_PROPOSED);
      return;
    }

    boolean deleted = evaluationRepository.deleteById(evaluation.getId());
    if (deleted) {
      reverted.add(formId);
    } else {
      // A avaliação existia no findById mas já não no deleteById -- corrida rara, tratada como
      // se nunca tivesse sido encontrada.
      blocked.put(formId, FormGenerationRevertSkipReason.EVALUATION_NOT_FOUND);
    }
  }

  /**
   * Resultado de um varrimento de reversão: os {@code generatedFormId} das avaliações apagadas, e
   * o motivo de cada uma que ficou por apagar.
   */
  public record RevertOutcome(List<UUID> revertedFormIds, Map<UUID, FormGenerationRevertSkipReason> blockedFormIds) {
  }
}
