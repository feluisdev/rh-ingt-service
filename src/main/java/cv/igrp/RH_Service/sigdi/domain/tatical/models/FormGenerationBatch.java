package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

/**
 * O rasto de um lote de geração de formulários na abertura de um período de submissão
 * (Fase 119, {@code PRZ-06}/{@code PRZ-07}). Regista o período de origem, quantos formulários
 * foram criados, quantos falharam e quantos foram saltados -- sem que a Fase 120 (a reversão de
 * lotes) precise de contar as linhas filhas para o saber.
 *
 * <p><strong>Diferença deliberada face a {@link PaaSubmissionPeriod}:</strong> os contadores e a
 * lista de itens deste agregado são mutáveis. Um lote é um acumulador durante o varrimento --
 * {@link #addItem(FormGenerationBatchItem)} é chamado uma vez por colaborador (ou unidade
 * saltada) resolvido, e só no fim {@link #finish(LocalDateTime)} fecha o lote e deriva o
 * estado final. Isto não é uma incoerência com a imutabilidade do vizinho: é a forma natural de
 * um objecto que existe precisamente para acumular durante uma operação em curso.
 */
@Getter
public class FormGenerationBatch {

    /** A finalidade cria formulários (ex.: {@code SIADAP}). */
    public static final String CREATES_FORMS = "CREATES_FORMS";
    /** A finalidade não cria nada; o lote regista apenas quem tem de agir (ex.: {@code PAA}). */
    public static final String READ_ONLY = "READ_ONLY";

    private final UUID id;
    private final UUID periodId;
    private final Purpose purpose;
    private final PaaLevel type;
    private final Integer year;
    private final String generationMode;
    private final boolean dryRun;
    private FormGenerationBatchStatus status;
    private int createdCount;
    private int failedCount;
    private int skippedCount;
    private int pendingCount;
    private final LocalDateTime generatedAt;
    private LocalDateTime finishedAt;
    private final String generatedBy;
    private final List<FormGenerationBatchItem> items;

    private FormGenerationBatch(UUID id, UUID periodId, Purpose purpose, PaaLevel type, Integer year,
                                 String generationMode, boolean dryRun, FormGenerationBatchStatus status,
                                 int createdCount, int failedCount, int skippedCount, int pendingCount,
                                 LocalDateTime generatedAt, LocalDateTime finishedAt, String generatedBy,
                                 List<FormGenerationBatchItem> items) {
        if (periodId == null) throw new IllegalArgumentException("periodId é obrigatório");
        if (purpose == null) throw new IllegalArgumentException("purpose é obrigatório");
        if (type == null) throw new IllegalArgumentException("type é obrigatório");
        if (year == null) throw new IllegalArgumentException("year é obrigatório");
        if (generatedBy == null || generatedBy.isBlank())
            throw new IllegalArgumentException("generatedBy é obrigatório");
        if (generatedAt == null) throw new IllegalArgumentException("generatedAt é obrigatório");

        this.id = id;
        this.periodId = periodId;
        this.purpose = purpose;
        this.type = type;
        this.year = year;
        this.generationMode = generationMode;
        this.dryRun = dryRun;
        this.status = status;
        this.createdCount = createdCount;
        this.failedCount = failedCount;
        this.skippedCount = skippedCount;
        this.pendingCount = pendingCount;
        this.generatedAt = generatedAt;
        this.finishedAt = finishedAt;
        this.generatedBy = generatedBy;
        this.items = new ArrayList<>(items != null ? items : List.of());
    }

    public static FormGenerationBatch start(UUID periodId, Purpose purpose, PaaLevel type, Integer year,
                                             String generationMode, boolean dryRun, String generatedBy,
                                             LocalDateTime generatedAt) {
        return new FormGenerationBatch(UUID.randomUUID(), periodId, purpose, type, year, generationMode,
                dryRun, null, 0, 0, 0, 0, generatedAt, null, generatedBy, List.of());
    }

    public static FormGenerationBatch reconstruct(UUID id, UUID periodId, Purpose purpose, PaaLevel type,
                                                    Integer year, String generationMode, boolean dryRun,
                                                    FormGenerationBatchStatus status, int createdCount,
                                                    int failedCount, int skippedCount, int pendingCount,
                                                    LocalDateTime generatedAt, LocalDateTime finishedAt,
                                                    String generatedBy, List<FormGenerationBatchItem> items) {
        return new FormGenerationBatch(id, periodId, purpose, type, year, generationMode, dryRun, status,
                createdCount, failedCount, skippedCount, pendingCount, generatedAt, finishedAt, generatedBy, items);
    }

    public List<FormGenerationBatchItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Acrescenta uma linha ao lote e incrementa só o contador que corresponde ao
     * {@code outcome} dela.
     */
    public void addItem(FormGenerationBatchItem item) {
        items.add(item);
        switch (item.getOutcome()) {
            case CREATED, WOULD_CREATE -> createdCount++;
            case FAILED -> failedCount++;
            case SKIPPED -> skippedCount++;
            case PENDING, ALREADY_EXISTED -> pendingCount++;
        }
    }

    /**
     * Fixa o instante de fim e deriva o {@link FormGenerationBatchStatus} a partir dos
     * contadores acumulados. Um lote fecha uma vez só -- uma segunda chamada é um erro de
     * programação de quem o chama, não um caso de negócio.
     */
    public void finish(LocalDateTime finishedAt) {
        if (this.finishedAt != null)
            throw new IllegalStateException("Lote já foi fechado em " + this.finishedAt);

        this.finishedAt = finishedAt;
        if (failedCount > 0 && createdCount > 0) {
            this.status = FormGenerationBatchStatus.PARTIAL;
        } else if (failedCount > 0) {
            this.status = FormGenerationBatchStatus.FAILED;
        } else if (READ_ONLY.equals(generationMode)) {
            this.status = FormGenerationBatchStatus.NOTHING_TO_GENERATE;
        } else if (dryRun) {
            this.status = FormGenerationBatchStatus.DRY_RUN;
        } else {
            this.status = FormGenerationBatchStatus.COMPLETED;
        }
    }
}
