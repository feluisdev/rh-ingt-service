package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.EligibilitySkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluatorSkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationDetailDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationItemDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Leitura completa do lote de geração de formulários de um período de submissão (Fase 119,
 * {@code PRZ-05}/{@code PRZ-07}) -- o modal do ecrã, aberto uma linha de cada vez (D-24,
 * 119-05-PLAN.md). A contagem em bloco de vários períodos é {@link GetPeriodGenerationSummariesQueryHandler}.
 *
 * <p><strong>D-26 -- "já submeteu" define-se por finalidade, e uma delas não é apurável.</strong>
 * Medido contra o esquema:
 * <ul>
 *   <li>{@code PAA} (nos dois níveis): uma unidade conta como submetida se tiver pelo menos uma
 *   linha em {@code t_tactical_activities} com {@code fiscal_year = ano} e
 *   {@code paa_level = tipo do período} -- {@link TacticalActivityRepository#findOrganicUnitIdsWithActivitiesInYear}.</li>
 *   <li>{@code SIADAP} e as três finalidades SIADAP de transição: submetido deriva do próprio
 *   {@code outcome} do item do lote ({@code CREATED} e {@code ALREADY_EXISTED} dão
 *   {@code true}) -- é o que o lote já regista, sem tocar no finder tático.</li>
 *   <li>{@code PAA_BSC_OBJECTIVES}: <strong>não apurável por unidade.</strong>
 *   {@code StrategicGoalEntity} tem {@code year} e {@code institution_id} e nenhuma coluna de
 *   unidade orgânica -- o artefacto é do organismo inteiro, o rol de elegíveis é por unidade.
 *   Não há como responder "que unidades ainda não submeteram" sem inventar uma ligação que não
 *   existe. {@code submissionCheck} vem {@code "NOT_APPLICABLE"} e todos os {@code submitted} da
 *   resposta vêm nulos; o finder tático nunca é chamado para esta finalidade. Isto é um limite
 *   nomeado e medido, não uma simplificação -- o que faltaria é uma dimensão de unidade no
 *   artefacto, desenho de outro milestone.</li>
 * </ul>
 *
 * <p><strong>D-27 -- "nunca gerado" é distinto de "gerado e vazio".</strong> Um período sem lote
 * nenhum devolve {@code batch} nulo, {@code batchId} nulo, {@code status = "NOT_GENERATED"} e
 * quatro listas vazias, sempre com {@code 200} -- nunca {@code 404} e nunca uma lista vazia
 * indistinguível de "gerado e vazio".
 *
 * <p><strong>D-28 -- o lote mostrado é o mais recente que não seja uma simulação.</strong> Se
 * houver pelo menos um lote com {@code dryRun = false}, mostra-se o mais recente desses; se só
 * houver simulações, mostra-se a mais recente e o DTO diz {@code dryRun = true}. Uma simulação
 * não pode passar por geração a sério no ecrã. A ordem vem do contrato de
 * {@link FormGenerationBatchRepository#findByPeriodId} ({@code generatedAt} descendente).
 *
 * <p><strong>D-29 -- sem paginação nestes endpoints.</strong> O universo é o número de
 * funcionários do organismo -- centenas, não milhares -- e paginar um rol que o ecrã mostra de
 * uma vez acrescenta estado sem resolver problema medido. Se este número deixar de valer, esta
 * decisão tem de ser revista.
 */
@Component
public class GetPeriodGenerationQueryHandler
        implements QueryHandler<GetPeriodGenerationQuery, ResponseEntity<FormGenerationDetailDTO>> {

    private final PaaSubmissionPeriodRepository periodRepository;
    private final FormGenerationBatchRepository batchRepository;
    private final TacticalActivityRepository tacticalActivityRepository;

    public GetPeriodGenerationQueryHandler(PaaSubmissionPeriodRepository periodRepository,
                                            FormGenerationBatchRepository batchRepository,
                                            TacticalActivityRepository tacticalActivityRepository) {
        this.periodRepository = periodRepository;
        this.batchRepository = batchRepository;
        this.tacticalActivityRepository = tacticalActivityRepository;
    }

    @IgrpQueryHandler
    @Override
    public ResponseEntity<FormGenerationDetailDTO> handle(GetPeriodGenerationQuery query) {
        PaaSubmissionPeriod period = periodRepository.findById(query.getPeriodId())
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Período de submissão não encontrado para o identificador " + query.getPeriodId()));

        Purpose purpose = period.getPurpose();
        PaaLevel type = period.getType();
        Integer year = period.getYear();

        FormGenerationDetailDTO dto = new FormGenerationDetailDTO();
        dto.setPeriodId(period.getId().toString());
        dto.setPurpose(purpose.getCode());
        dto.setPurposeDesc(purpose.getDescription());
        dto.setType(type.getCode());
        dto.setTypeDesc(type.getDescription());
        dto.setYear(year);
        dto.setSubmissionCheck(resolveSubmissionCheck(purpose));

        List<FormGenerationBatch> batches = batchRepository.findByPeriodId(period.getId());
        Optional<FormGenerationBatch> selected = selectMostRecentNonDryRun(batches);

        if (selected.isEmpty()) {
            // D-27: nunca gerado -- distinto de "gerado e vazio".
            dto.setStatus("NOT_GENERATED");
            dto.setBatchId(null);
            dto.setDryRun(false);
            return ResponseEntity.ok(dto);
        }

        FormGenerationBatch batch = selected.get();
        dto.setBatchId(batch.getId().toString());
        dto.setStatus(batch.getStatus() != null ? batch.getStatus().getCode() : null);
        dto.setGenerationMode(batch.getGenerationMode());
        dto.setCreatedCount(batch.getCreatedCount());
        dto.setFailedCount(batch.getFailedCount());
        dto.setSkippedCount(batch.getSkippedCount());
        dto.setPendingCount(batch.getPendingCount());
        dto.setGeneratedAt(batch.getGeneratedAt());
        dto.setGeneratedBy(batch.getGeneratedBy());
        dto.setDryRun(batch.isDryRun());

        // O finder tático só é chamado para PAA (D-26) -- nunca para SIADAP (o outcome já
        // responde) nem para PAA_BSC_OBJECTIVES (não apurável).
        Set<UUID> submittedUnits = (purpose == Purpose.PAA)
                ? new HashSet<>(tacticalActivityRepository.findOrganicUnitIdsWithActivitiesInYear(year, type))
                : Set.of();

        for (FormGenerationBatchItem item : batch.getItems()) {
            FormGenerationItemDTO itemDTO = toItemDTO(item, purpose, submittedUnits);
            switch (item.getOutcome()) {
                case CREATED, WOULD_CREATE, ALREADY_EXISTED -> dto.getCreated().add(itemDTO);
                case FAILED -> dto.getFailed().add(itemDTO);
                case SKIPPED -> dto.getSkipped().add(itemDTO);
                case PENDING -> dto.getPending().add(itemDTO);
            }
        }

        return ResponseEntity.ok(dto);
    }

    /**
     * D-28: o lote a mostrar é o mais recente cujo {@code dryRun} seja {@code false}; se todos
     * forem simulações, o mais recente de qualquer forma. {@code batches} chega já ordenado por
     * {@code generatedAt} descendente (contrato de {@link FormGenerationBatchRepository#findByPeriodId}),
     * por isso {@code findFirst()} preserva essa ordem. Extraído como {@code static} para que
     * {@link GetPeriodGenerationSummariesQueryHandler} use exactamente o mesmo critério -- duas
     * regras de escolha que divergissem fariam a coluna e o modal contradizerem-se.
     */
    static Optional<FormGenerationBatch> selectMostRecentNonDryRun(List<FormGenerationBatch> batches) {
        if (batches == null || batches.isEmpty()) {
            return Optional.empty();
        }
        return batches.stream()
                .filter(batch -> !batch.isDryRun())
                .findFirst()
                .or(() -> Optional.of(batches.get(0)));
    }

    /**
     * Switch expression sobre {@link Purpose} sem {@code default} (mesmo padrão da Fase 116):
     * um {@code Purpose} novo quebra a compilação em vez de cair num ramo silencioso que minta
     * sobre a apurabilidade.
     */
    private static String resolveSubmissionCheck(Purpose purpose) {
        return switch (purpose) {
            case PAA, SIADAP, SIADAP_INTERIM, SIADAP_SELF_EVAL, SIADAP_FINAL -> "APPLICABLE";
            case PAA_BSC_OBJECTIVES -> "NOT_APPLICABLE";
        };
    }

    private static Boolean resolveSubmitted(Purpose purpose, FormGenerationBatchItem item,
                                             Set<UUID> submittedUnits) {
        return switch (purpose) {
            case PAA -> item.getUnitId() != null && submittedUnits.contains(item.getUnitId());
            case PAA_BSC_OBJECTIVES -> null;
            case SIADAP, SIADAP_INTERIM, SIADAP_SELF_EVAL, SIADAP_FINAL ->
                    item.getOutcome() == cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome.CREATED
                            || item.getOutcome() == cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome.ALREADY_EXISTED;
        };
    }

    /**
     * A descrição do {@code skipReason} tenta primeiro {@link EligibilitySkipReason#fromCode},
     * depois {@link EvaluatorSkipReason#fromCode} -- a coluna guarda o código de um dos dois
     * enums (VARCHAR livre, ver Javadoc de {@code FormGenerationOutcome}). Nunca
     * {@code fromCodeOrThrow}: um código que o ecrã não reconhece não pode transformar uma
     * leitura num erro -- devolve o próprio código como descrição.
     */
    private static String resolveSkipReasonDescription(String skipReason) {
        if (skipReason == null) {
            return null;
        }
        return EligibilitySkipReason.fromCode(skipReason)
                .map(EligibilitySkipReason::getDescription)
                .or(() -> EvaluatorSkipReason.fromCode(skipReason).map(EvaluatorSkipReason::getDescription))
                .orElse(skipReason);
    }

    private static FormGenerationItemDTO toItemDTO(FormGenerationBatchItem item, Purpose purpose,
                                                     Set<UUID> submittedUnits) {
        FormGenerationItemDTO dto = new FormGenerationItemDTO();
        dto.setEmployeeId(item.getEmployeeId() != null ? item.getEmployeeId().toString() : null);
        dto.setEmployeeName(item.getEmployeeName());
        dto.setUnitId(item.getUnitId() != null ? item.getUnitId().toString() : null);
        dto.setUnitName(item.getUnitName());
        dto.setOutcome(item.getOutcome().getCode());
        dto.setOutcomeDescription(item.getOutcome().getDescription());
        dto.setGeneratedFormId(item.getGeneratedFormId() != null ? item.getGeneratedFormId().toString() : null);
        dto.setEvaluatorId(item.getEvaluatorId() != null ? item.getEvaluatorId().toString() : null);
        dto.setSkipReason(item.getSkipReason());
        dto.setSkipReasonDescription(resolveSkipReasonDescription(item.getSkipReason()));
        dto.setErrorMessage(item.getErrorMessage());
        dto.setSubmitted(resolveSubmitted(purpose, item, submittedUnits));
        return dto;
    }
}
