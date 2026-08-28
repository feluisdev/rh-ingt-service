package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.config.SystemAuditor;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsibleDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsiblesDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleUnitGroupDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SkippedUnitDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Despacha a geração de formulários na abertura de um período de submissão, pelas sete
 * combinações reais de {@code (Purpose, PaaLevel)} (Fase 119, {@code PRZ-01}/{@code PRZ-06}):
 *
 * <table>
 *   <caption>Sete combinações de despacho</caption>
 *   <tr><th>Purpose</th><th>PaaLevel</th><th>Modo</th><th>O que gera</th></tr>
 *   <tr><td>{@code SIADAP}</td><td>{@code INDIVIDUAL_LEVEL}</td><td>{@code CREATES_FORMS}</td>
 *       <td>uma {@code SiadapEvaluation} por colaborador elegível, via {@link SiadapFormGenerator}</td></tr>
 *   <tr><td>{@code PAA}</td><td>{@code UNIT_LEVEL}</td><td>{@code READ_ONLY}</td>
 *       <td>nada -- lote com {@code PENDING} por elegível e {@code SKIPPED} por unidade saltada</td></tr>
 *   <tr><td>{@code PAA}</td><td>{@code INDIVIDUAL_LEVEL}</td><td>{@code READ_ONLY}</td><td>idem</td></tr>
 *   <tr><td>{@code PAA_BSC_OBJECTIVES}</td><td>{@code UNIT_LEVEL} (marcador)</td><td>{@code READ_ONLY}</td><td>idem</td></tr>
 *   <tr><td>{@code SIADAP_INTERIM}</td><td>{@code INDIVIDUAL_LEVEL}</td><td>{@code READ_ONLY}</td><td>idem</td></tr>
 *   <tr><td>{@code SIADAP_SELF_EVAL}</td><td>{@code INDIVIDUAL_LEVEL}</td><td>{@code READ_ONLY}</td><td>idem</td></tr>
 *   <tr><td>{@code SIADAP_FINAL}</td><td>{@code INDIVIDUAL_LEVEL}</td><td>{@code READ_ONLY}</td><td>idem</td></tr>
 * </table>
 *
 * <p><strong>D-13 -- porque as seis combinações que não criam nada gravam lote na mesma.</strong>
 * O lote em modo {@code READ_ONLY} não é um efeito colateral dispensável: é o instantâneo que
 * responde ao {@code PRZ-05} para o {@code PAA} -- "estas unidades ainda não submeteram" -- e a
 * Fase 116 já declarou explicitamente que gravar esse instantâneo era trabalho desta fase, não
 * da dela. Omitir o lote nessas seis combinações deixaria o {@code PRZ-05} sem resposta.
 */
@Component
@RequiredArgsConstructor
public class PeriodFormGenerationService {

    private static final String SYSTEM_AUDITOR_FALLBACK = "system";

    private final EligibleResponsiblesResolver eligibleResponsiblesResolver;
    private final SiadapFormGenerator siadapFormGenerator;
    private final FormGenerationBatchRepository formGenerationBatchRepository;

    /**
     * Gera (ou simula, ou apenas regista quem tem de agir) para {@code period}, segundo a sua
     * combinação {@code (Purpose, PaaLevel)}, e grava o lote uma única vez, depois de fechado.
     *
     * <p><strong>Deliberadamente sem {@code @Transactional} neste método.</strong> Provado
     * contra base real na Fase 117 (ver {@code OBSERVACAO-BASE-REAL.md} dessa fase): o
     * {@code save} do adaptador já é transaccional por si (molde de
     * {@code PaaSubmissionPeriodRepositoryImpl}). Uma transacção única a envolver todo o
     * varrimento adiaria o {@code flush} do {@code AuditingEntityListener} para o fim -- depois
     * de {@link SystemAuditor#runAs(String, Runnable)} já ter reposto o {@code ThreadLocal} do
     * autor -- e a falha de um item arrastaria os restantes, exactamente o que o {@code D-12}
     * (falha por item, nunca por lote) existe para impedir. Aqui isto é ainda mais directo: o
     * lote é gravado uma única vez, no fim, já fechado por {@link FormGenerationBatch#finish} --
     * não há vários {@code save} por item a proteger, só o único {@code save} final, que corre
     * fora de qualquer âmbito transaccional próprio do método.
     *
     * <p>A excepção do {@link EligibleResponsiblesResolver} é o único caso em que este método
     * deixa passar sem capturar: se não se sabe sobre quem iterar, não há lote nenhum com
     * sentido para gravar.
     *
     * @param period período de submissão que acabou de abrir
     * @param dryRun se {@code true}, o lote é uma simulação -- ver {@link SiadapFormGenerator}
     * @return o lote gravado, já fechado
     */
    public FormGenerationBatch generateFor(PaaSubmissionPeriod period, boolean dryRun) {
        EligibleResponsiblesDTO eligible = eligibleResponsiblesResolver.resolve(period);

        // Switch expression sem default, de propósito (molde estabelecido pelo
        // EligibleResponsiblesResolver, Fase 116): um Purpose novo sem decisão de modo quebra a
        // compilação, em vez de cair num ramo silencioso.
        String mode = switch (period.getPurpose()) {
            case SIADAP -> FormGenerationBatch.CREATES_FORMS;
            case PAA, PAA_BSC_OBJECTIVES, SIADAP_INTERIM, SIADAP_SELF_EVAL, SIADAP_FINAL -> FormGenerationBatch.READ_ONLY;
        };

        LocalDateTime now = LocalDateTime.now(AppTimeZone.CABO_VERDE);
        String generatedBy = SystemAuditor.current().orElse(SYSTEM_AUDITOR_FALLBACK);

        FormGenerationBatch batch = FormGenerationBatch.start(period.getId(), period.getPurpose(), period.getType(),
                period.getYear(), mode, dryRun, generatedBy, now);

        if (FormGenerationBatch.CREATES_FORMS.equals(mode)) {
            List<FormGenerationBatchItem> items = siadapFormGenerator.generate(eligible, period.getYear(), dryRun, now);
            items.forEach(batch::addItem);
        } else {
            addReadOnlyItems(batch, eligible, now);
        }

        batch.finish(LocalDateTime.now(AppTimeZone.CABO_VERDE));
        return formGenerationBatchRepository.save(batch);
    }

    /**
     * D-13: as seis combinações {@code READ_ONLY} não criam nada, mas o lote grava na mesma o
     * instantâneo de quem tem de agir -- {@code PENDING} por elegível, {@code SKIPPED} por
     * unidade saltada pela Fase 116.
     */
    private void addReadOnlyItems(FormGenerationBatch batch, EligibleResponsiblesDTO eligible, LocalDateTime now) {
        for (EligibleUnitGroupDTO group : eligible.getGroups()) {
            UUID unitUuid = UUID.fromString(group.getUnitId());
            for (EligibleResponsibleDTO responsible : group.getResponsibles()) {
                UUID employeeUuid = UUID.fromString(responsible.getEmployeeId());
                batch.addItem(FormGenerationBatchItem.of(employeeUuid, responsible.getEmployeeName(), unitUuid,
                        group.getUnitName(), FormGenerationOutcome.PENDING, null, null, null, null, now));
            }
        }
        for (SkippedUnitDTO skippedUnit : eligible.getSkipped()) {
            UUID unitUuid = UUID.fromString(skippedUnit.getUnitId());
            batch.addItem(FormGenerationBatchItem.of(null, null, unitUuid, skippedUnit.getUnitName(),
                    FormGenerationOutcome.SKIPPED, null, null, skippedUnit.getReason(), null, now));
        }
    }
}
