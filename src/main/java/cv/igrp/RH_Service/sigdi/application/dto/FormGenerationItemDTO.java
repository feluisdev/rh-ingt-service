// Mantido à mão, não gerado pelo iGRP Studio: DTO de saída da leitura do lote de geração de
// formulários (Fase 119, PRZ-05/PRZ-07), sem manifesto correspondente em .igrpstudio/sigdi/dto/.
// Ver 119-05-PLAN.md.
package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FormGenerationItemDTO {

    private String employeeId;

    private String employeeName;

    private String unitId;

    private String unitName;

    // Código de FormGenerationOutcome, como String -- mesmo precedente de SkippedUnitDTO#reason
    // (Fase 116): não amarra a serialização JSON à forma do enum.
    private String outcome;

    private String outcomeDescription;

    private String generatedFormId;

    private String evaluatorId;

    // Código de EligibilitySkipReason ou de EvaluatorSkipReason -- coluna VARCHAR livre (ver
    // Javadoc de FormGenerationOutcome), nulo quando o item não foi saltado.
    private String skipReason;

    private String skipReasonDescription;

    private String errorMessage;

    // Anulável de propósito: nulo significa "não apurável" (finalidade PAA_BSC_OBJECTIVES, D-26
    // de 119-05-PLAN.md -- StrategicGoalEntity não tem coluna de unidade orgânica).
    private Boolean submitted;

    // Fase 120, plano 03 (PRZ-04): nulo enquanto este item específico não foi desfeito. Um item
    // CREATED já desfeito continua na lista "created" do FormGenerationDetailDTO -- não se tira
    // de lá, só se marca aqui (ver o comentário no GetPeriodGenerationQueryHandler).
    private LocalDateTime revertedAt;

    // Código de FormGenerationRevertSkipReason, como String -- mesmo precedente de #outcome
    // acima. Nulo quando o item nunca foi bloqueado numa reversão.
    private String revertSkipReason;

    private String revertSkipReasonDescription;
}
