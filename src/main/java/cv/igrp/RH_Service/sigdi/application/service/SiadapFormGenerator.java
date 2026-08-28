package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsibleDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsiblesDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleUnitGroupDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SkippedUnitDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.SiadapConfig;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.SiadapConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Gerador de {@link SiadapEvaluation} por colaborador elegível, disparado pela abertura de um
 * período {@code SIADAP}/{@code INDIVIDUAL_LEVEL} (Fase 119, {@code PRZ-01}).
 *
 * <p><strong>D-10 -- porque este gerador não passa pelo
 * {@code CreateSiadapEvaluationCommandHandler}.</strong> Esse handler <strong>ignora
 * deliberadamente</strong> o {@code evaluatorId} do pedido (T-109-13, decisão de repúdio da
 * Fase 109, ver o seu Javadoc) e deriva o avaliador de
 * {@code funcionarioLookupPort.findCurrentOrganizationalUnitId} -- "onde o colaborador está
 * hoje". Este gerador precisa de "onde o colaborador esteve no ano do período" -- a pergunta
 * que a Fase 116 já respondeu ao produzir {@link EligibleResponsiblesDTO} -- e o
 * {@link EvaluatorResolver} (plano 02) deriva exactamente essa unidade, com subida à
 * unidade-mãe quando o responsável é o próprio elegível. Passar esse avaliador ao handler não
 * funcionaria (seria descartado e substituído em silêncio, só com um {@code LOGGER.warn}); forçar
 * o handler a aceitá-lo desfaria uma decisão de segurança de outra fase. Por isso este gerador
 * chama {@link SiadapEvaluation#create} e {@link SiadapEvaluationRepository#save} directamente,
 * e replica só as duas coisas do handler que importam para a coerência dos dados: a guarda de
 * duplicado ({@code findByEmployeeAndYear}) e a leitura de pesos de
 * {@link SiadapConfigRepository#findByFiscalYear} com omissão 60/40 (D-16).
 *
 * <p>Este gerador <strong>não cria nem grava o lote</strong> -- devolve a lista de itens que o
 * chamador ({@code PeriodFormGenerationService}, plano 03 Task 3) acumula num
 * {@code FormGenerationBatch}.
 */
@Component
@RequiredArgsConstructor
public class SiadapFormGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiadapFormGenerator.class);

    private static final BigDecimal DEFAULT_RESULTS_WEIGHT = new BigDecimal("60");
    private static final BigDecimal DEFAULT_COMPETENCIES_WEIGHT = new BigDecimal("40");

    private final SiadapEvaluationRepository evaluationRepository;
    private final SiadapConfigRepository configRepository;
    private final EvaluatorResolver evaluatorResolver;

    /**
     * Percorre {@code eligible}, cria uma {@link SiadapEvaluation} por colaborador elegível sem
     * avaliação nesse ano e com avaliador derivável, e devolve um item por colaborador (ou
     * unidade saltada pela Fase 116).
     *
     * <p>{@code now} entra por parâmetro e não é lido dentro deste método -- assim o teste fixa
     * o instante sem congelar o relógio, e é o chamador quem decide a fonte (em produção,
     * {@code LocalDateTime.now(AppTimeZone.CABO_VERDE)}).
     *
     * @param eligible instantâneo de elegíveis do período (Fase 116)
     * @param year ano do período, usado para a guarda de idempotência e para ler os pesos
     * @param dryRun se {@code true}, percorre tudo e deriva tudo, mas nunca chama
     *               {@link SiadapEvaluationRepository#save}
     * @param now instante gravado em cada item devolvido
     */
    public List<FormGenerationBatchItem> generate(EligibleResponsiblesDTO eligible, Integer year,
                                                    boolean dryRun, LocalDateTime now) {
        List<FormGenerationBatchItem> items = new ArrayList<>();

        // Pesos lidos uma vez por lote, não por colaborador (D-16) -- uma avaliação gerada e
        // uma criada à mão no mesmo ano ficam indistinguíveis nos pesos.
        Optional<SiadapConfig> configOpt = configRepository.findByFiscalYear(year);
        BigDecimal resultsWeight = configOpt.map(SiadapConfig::getResultsWeight).orElse(DEFAULT_RESULTS_WEIGHT);
        BigDecimal competenciesWeight = configOpt.map(SiadapConfig::getCompetenciesWeight).orElse(DEFAULT_COMPETENCIES_WEIGHT);

        for (EligibleUnitGroupDTO group : eligible.getGroups()) {
            for (EligibleResponsibleDTO responsible : group.getResponsibles()) {
                items.add(processResponsible(group, responsible, year, dryRun, now, resultsWeight, competenciesWeight));
            }
        }

        for (SkippedUnitDTO skippedUnit : eligible.getSkipped()) {
            items.add(skippedUnitItem(skippedUnit, now));
        }

        return items;
    }

    private FormGenerationBatchItem processResponsible(EligibleUnitGroupDTO group, EligibleResponsibleDTO responsible,
                                                         Integer year, boolean dryRun, LocalDateTime now,
                                                         BigDecimal resultsWeight, BigDecimal competenciesWeight) {
        String employeeIdRaw = responsible.getEmployeeId();
        UUID employeeUuid = null;
        UUID unitUuid = null;
        try {
            // D-11: a guarda de idempotência real é por colaborador (findByEmployeeAndYear),
            // não por lote -- mas primeiro é preciso saber se há avaliador, por ordem do
            // action deste plano.
            employeeUuid = UUID.fromString(employeeIdRaw);
            unitUuid = UUID.fromString(group.getUnitId());

            EvaluatorResolver.EvaluatorResolution resolution = evaluatorResolver.resolve(employeeUuid, unitUuid);
            if (resolution.isSkipped()) {
                return FormGenerationBatchItem.of(employeeUuid, responsible.getEmployeeName(), unitUuid, group.getUnitName(),
                        FormGenerationOutcome.SKIPPED, null, null, resolution.skipReason().getCode(), null, now);
            }

            Optional<SiadapEvaluation> existing = evaluationRepository.findByEmployeeAndYear(employeeIdRaw, year);
            if (existing.isPresent()) {
                UUID existingId = UUID.fromString(existing.get().getId().getStringValor());
                return FormGenerationBatchItem.of(employeeUuid, responsible.getEmployeeName(), unitUuid, group.getUnitName(),
                        FormGenerationOutcome.ALREADY_EXISTED, existingId, resolution.evaluatorId(), null, null, now);
            }

            if (dryRun) {
                // D-14: a simulação verifica de verdade (duplicado e avaliador já foram
                // resolvidos acima) -- só não chama save().
                return FormGenerationBatchItem.of(employeeUuid, responsible.getEmployeeName(), unitUuid, group.getUnitName(),
                        FormGenerationOutcome.WOULD_CREATE, null, resolution.evaluatorId(), null, null, now);
            }

            // D-15: organicUnitId é o unitId do grupo elegível -- a unidade cujo enquadramento
            // no ano do período tornou a pessoa elegível -- e não a unidade actual.
            SiadapEvaluation evaluation = SiadapEvaluation.create(employeeIdRaw, year, group.getUnitId(),
                    resolution.evaluatorId().toString(), resultsWeight, competenciesWeight);
            SiadapEvaluation saved = evaluationRepository.save(evaluation);
            UUID savedId = UUID.fromString(saved.getId().getStringValor());
            return FormGenerationBatchItem.of(employeeUuid, responsible.getEmployeeName(), unitUuid, group.getUnitName(),
                    FormGenerationOutcome.CREATED, savedId, resolution.evaluatorId(), null, null, now);
        } catch (Exception e) {
            // D-12: falha por item, nunca por lote -- LOGGER.error e uma linha FAILED com
            // errorMessage, as duas, molde do PaaSubmissionPeriodExpiryScheduler (Fase 117).
            LOGGER.error("Falha ao gerar avaliação SIADAP para o colaborador '{}': {}", employeeIdRaw, e.getMessage(), e);
            String errorMessage = "Falha ao gerar avaliação SIADAP para o colaborador '" + employeeIdRaw + "': " + e.getMessage();
            return FormGenerationBatchItem.of(employeeUuid, responsible.getEmployeeName(), unitUuid, group.getUnitName(),
                    FormGenerationOutcome.FAILED, null, null, null, errorMessage, now);
        }
    }

    private FormGenerationBatchItem skippedUnitItem(SkippedUnitDTO skippedUnit, LocalDateTime now) {
        UUID unitUuid = UUID.fromString(skippedUnit.getUnitId());
        return FormGenerationBatchItem.of(null, null, unitUuid, skippedUnit.getUnitName(),
                FormGenerationOutcome.SKIPPED, null, null, skippedUnit.getReason(), null, now);
    }
}
