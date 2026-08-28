package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationRevertSkipReason;
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
    // Fase 120, plano 01 (PRZ-04): preenchidos só pelo update em bloco de
    // FormGenerationBatchRepositoryImpl.markReverted (plano 120-01, tarefa 3) -- nunca por
    // esta classe. of() continua a produzir os dois a nulo; quem os grava depois da criação
    // é a reversão, não o agregado.
    private final LocalDateTime revertedAt;
    private final FormGenerationRevertSkipReason revertSkipReason;

    private FormGenerationBatchItem(UUID employeeId, String employeeName, UUID unitId, String unitName,
                                     FormGenerationOutcome outcome, UUID generatedFormId, UUID evaluatorId,
                                     String skipReason, String errorMessage, LocalDateTime createdAt,
                                     LocalDateTime revertedAt, FormGenerationRevertSkipReason revertSkipReason) {
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
        this.revertedAt = revertedAt;
        this.revertSkipReason = revertSkipReason;
    }

    /**
     * {@code employeeId} é anulável de propósito: uma linha de unidade orgânica saltada (por
     * exemplo {@code UNIT_WITHOUT_RESPONSIBLE}) não tem colaborador nenhum a apontar.
     *
     * <p>Assinatura inalterada desde a Fase 119 -- é chamada em seis sítios do
     * {@code SiadapFormGenerator} e do {@code PeriodFormGenerationService}, e mudá-la arrastaria
     * essa fase para dentro da 120 sem necessidade. Os dois campos de reversão nascem sempre a
     * nulo aqui; só {@link #reconstruct} os preenche, e só o mapper o chama.
     */
    public static FormGenerationBatchItem of(UUID employeeId, String employeeName, UUID unitId, String unitName,
                                              FormGenerationOutcome outcome, UUID generatedFormId, UUID evaluatorId,
                                              String skipReason, String errorMessage, LocalDateTime createdAt) {
        return new FormGenerationBatchItem(employeeId, employeeName, unitId, unitName, outcome,
                generatedFormId, evaluatorId, skipReason, errorMessage, createdAt, null, null);
    }

    /**
     * Reconstitui um item a partir da persistência, com os dois campos de reversão -- usado só
     * pelo {@code FormGenerationBatchMapper}.
     */
    public static FormGenerationBatchItem reconstruct(UUID employeeId, String employeeName, UUID unitId,
                                                        String unitName, FormGenerationOutcome outcome,
                                                        UUID generatedFormId, UUID evaluatorId, String skipReason,
                                                        String errorMessage, LocalDateTime createdAt,
                                                        LocalDateTime revertedAt,
                                                        FormGenerationRevertSkipReason revertSkipReason) {
        return new FormGenerationBatchItem(employeeId, employeeName, unitId, unitName, outcome,
                generatedFormId, evaluatorId, skipReason, errorMessage, createdAt, revertedAt, revertSkipReason);
    }
}
