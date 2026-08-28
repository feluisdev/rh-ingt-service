// Mantido à mão, não gerado pelo iGRP Studio: DTO de saída de um item mantido (não apagado) ao
// desfazer um lote de geração de formulários (Fase 120, plano 03, PRZ-04), sem manifesto
// correspondente em .igrpstudio/sigdi/dto/. Mesmo precedente dos três DTOs de leitura da Fase
// 119 (FormGenerationSummaryDTO, FormGenerationDetailDTO, FormGenerationItemDTO).
package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FormGenerationRevertSkippedItemDTO {

    private String employeeId;

    private String employeeName;

    private String unitId;

    private String unitName;

    private String generatedFormId;

    // Código de FormGenerationRevertSkipReason, como String -- mesmo precedente de
    // SkippedUnitDTO#reason (Fase 116) e de FormGenerationItemDTO#outcome (Fase 119): não amarra
    // a serialização JSON à forma do enum.
    private String reason;

    private String reasonDescription;
}
