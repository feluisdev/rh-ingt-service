package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

/**
 * Uma linha do rasto de um {@link FormGenerationBatch}: o que aconteceu a um colaborador, ou a
 * uma unidade orgânica saltada, dentro de um lote de geração de formulários (Fase 119,
 * {@code PRZ-06}/{@code PRZ-07}).
 *
 * <p>Agregado filho simples, sem comportamento além da validação de construção -- não muda de
 * estado depois de criado.
 */
@Getter
public class FormGenerationBatchItem {

    private final UUID employeeId;
    private final String employeeName;
    private final UUID unitId;
    private final String unitName;
    private final FormGenerationOutcome outcome;
    private final UUID generatedFormId;
    private final UUID evaluatorId;
    private final String skipReason;
    private final String errorMessage;
    private final LocalDateTime createdAt;

    private FormGenerationBatchItem(UUID employeeId, String employeeName, UUID unitId, String unitName,
                                     FormGenerationOutcome outcome, UUID generatedFormId, UUID evaluatorId,
                                     String skipReason, String errorMessage, LocalDateTime createdAt) {
        if (outcome == null) throw new IllegalArgumentException("outcome é obrigatório");
        if (createdAt == null) throw new IllegalArgumentException("createdAt é obrigatório");
        // Uma falha sem razão escrita é o modo de falha que o PRZ-07 existe para impedir: uma
        // tentativa que falhou tem de deixar sempre um motivo legível, e não apenas uma linha
        // de log que desaparece.
        if (outcome == FormGenerationOutcome.FAILED && (errorMessage == null || errorMessage.isBlank()))
            throw new IllegalArgumentException("errorMessage é obrigatório quando outcome é FAILED");

        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.unitId = unitId;
        this.unitName = unitName;
        this.outcome = outcome;
        this.generatedFormId = generatedFormId;
        this.evaluatorId = evaluatorId;
        this.skipReason = skipReason;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    /**
     * {@code employeeId} é anulável de propósito: uma linha de unidade orgânica saltada (por
     * exemplo {@code UNIT_WITHOUT_RESPONSIBLE}) não tem colaborador nenhum a apontar.
     */
    public static FormGenerationBatchItem of(UUID employeeId, String employeeName, UUID unitId, String unitName,
                                              FormGenerationOutcome outcome, UUID generatedFormId, UUID evaluatorId,
                                              String skipReason, String errorMessage, LocalDateTime createdAt) {
        return new FormGenerationBatchItem(employeeId, employeeName, unitId, unitName, outcome,
                generatedFormId, evaluatorId, skipReason, errorMessage, createdAt);
    }
}
