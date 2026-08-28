package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationRevertSkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchItemEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FormGenerationBatchMapper {

    // Fase 119, plano 08 (PRZ-07). Alinhar o esquema (V35) nao chega: uma mensagem de excepcao
    // continua a poder exceder o que a coluna aceite noutro ambiente que ainda nao tenha essa
    // migracao, ou simplesmente crescer sem limite (causas encadeadas concatenadas). O modo de
    // falha e o pior possivel -- o INSERT do item falha em commit e arrasta consigo o lote
    // inteiro, incluindo itens que correram bem (medido no 119-07). Truncagem defensiva aqui,
    // na fronteira entre o dominio e a entidade JPA, garante que a gravacao nunca depende do
    // comprimento de uma mensagem que o codigo nao controla.
    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;
    private static final String TRUNCATION_MARKER = "[...truncado]";

    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH) return errorMessage;

        int keep = MAX_ERROR_MESSAGE_LENGTH - TRUNCATION_MARKER.length();
        return errorMessage.substring(0, keep) + TRUNCATION_MARKER;
    }

    public FormGenerationBatch toDomain(FormGenerationBatchEntity entity, List<FormGenerationBatchItemEntity> itemEntities) {
        if (entity == null) return null;

        List<FormGenerationBatchItem> items = (itemEntities == null) ? List.of() :
                itemEntities.stream().map(this::toItemDomain).toList();

        FormGenerationBatchStatus status = (entity.getStatus() != null)
                ? FormGenerationBatchStatus.fromCodeOrThrow(entity.getStatus())
                : null;

        // dryRun nao e coluna persistida (V34) -- o estado DRY_RUN e derivado uma unica vez no
        // finish() do agregado e fica gravado no proprio status; ao reconstruir a partir da
        // base, o unico facto que ainda importa e esse status ja fixado. O booleano aqui e
        // reconstituido a partir dele so para preencher o campo, nao para ser recalculado.
        boolean dryRun = status == FormGenerationBatchStatus.DRY_RUN;

        return FormGenerationBatch.reconstruct(
                entity.getId(),
                entity.getPeriodId(),
                Purpose.fromCodeOrThrow(entity.getPurpose()),
                PaaLevel.fromCodeOrThrow(entity.getType()),
                entity.getYear(),
                entity.getGenerationMode(),
                dryRun,
                status,
                entity.getCreatedCount() != null ? entity.getCreatedCount() : 0,
                entity.getFailedCount() != null ? entity.getFailedCount() : 0,
                entity.getSkippedCount() != null ? entity.getSkippedCount() : 0,
                entity.getPendingCount() != null ? entity.getPendingCount() : 0,
                entity.getGeneratedAt(),
                entity.getFinishedAt(),
                entity.getGeneratedBy(),
                items,
                entity.getRevertedAt(),
                entity.getRevertedBy(),
                entity.getRevertedCount() != null ? entity.getRevertedCount() : 0,
                entity.getRevertBlockedCount() != null ? entity.getRevertBlockedCount() : 0
        );
    }

    public FormGenerationBatchEntity toEntity(FormGenerationBatch domain) {
        if (domain == null) return null;

        FormGenerationBatchEntity entity = new FormGenerationBatchEntity();
        entity.setId(domain.getId());
        entity.setPeriodId(domain.getPeriodId());
        entity.setPurpose(domain.getPurpose().getCode());
        entity.setType(domain.getType().getCode());
        entity.setYear(domain.getYear());
        entity.setGenerationMode(domain.getGenerationMode());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().getCode() : null);
        entity.setCreatedCount(domain.getCreatedCount());
        entity.setFailedCount(domain.getFailedCount());
        entity.setSkippedCount(domain.getSkippedCount());
        entity.setPendingCount(domain.getPendingCount());
        entity.setGeneratedAt(domain.getGeneratedAt());
        entity.setFinishedAt(domain.getFinishedAt());
        entity.setGeneratedBy(domain.getGeneratedBy());
        return entity;
    }

    public FormGenerationBatchItem toItemDomain(FormGenerationBatchItemEntity entity) {
        if (entity == null) return null;

        // revert_skip_reason so existe depois de um markReverted que bloqueou este item -- nulo
        // e o caso normal, e fromCodeOrThrow so corre quando ha mesmo um codigo a validar,
        // exactamente como ja acontece com outcome.
        FormGenerationRevertSkipReason revertSkipReason = (entity.getRevertSkipReason() != null)
                ? FormGenerationRevertSkipReason.fromCodeOrThrow(entity.getRevertSkipReason())
                : null;

        return FormGenerationBatchItem.reconstruct(
                entity.getEmployeeId(),
                entity.getEmployeeName(),
                entity.getUnitId(),
                entity.getUnitName(),
                FormGenerationOutcome.fromCodeOrThrow(entity.getOutcome()),
                entity.getGeneratedFormId(),
                entity.getEvaluatorId(),
                entity.getSkipReason(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getRevertedAt(),
                revertSkipReason
        );
    }

    public FormGenerationBatchItemEntity toItemEntity(FormGenerationBatchItem item, UUID batchId) {
        if (item == null) return null;

        FormGenerationBatchItemEntity entity = new FormGenerationBatchItemEntity();
        entity.setId(UUID.randomUUID());
        entity.setBatchId(batchId);
        entity.setEmployeeId(item.getEmployeeId());
        entity.setEmployeeName(item.getEmployeeName());
        entity.setUnitId(item.getUnitId());
        entity.setUnitName(item.getUnitName());
        entity.setOutcome(item.getOutcome().getCode());
        entity.setGeneratedFormId(item.getGeneratedFormId());
        entity.setEvaluatorId(item.getEvaluatorId());
        entity.setSkipReason(item.getSkipReason());
        entity.setErrorMessage(truncateErrorMessage(item.getErrorMessage()));
        entity.setCreatedAt(item.getCreatedAt());
        return entity;
    }
}
