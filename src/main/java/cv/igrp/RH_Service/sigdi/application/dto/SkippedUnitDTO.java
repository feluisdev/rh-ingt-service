// Mantido à mão, não gerado pelo iGRP Studio: DTO de saída do EligibleResponsiblesResolver
// (Fase 116), sem manifesto correspondente em .igrpstudio/sigdi/dto/. Ver 116-03-PLAN.md.
package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class SkippedUnitDTO {

    private String unitId;

    private String unitName;

    // Código de EligibilitySkipReason, como String e não como o próprio enum -- não amarra
    // a serialização JSON à forma do enum, seguindo o precedente de type/purpose em
    // PaaSubmissionPeriodResponseDTO.
    private String reason;

    private String reasonDescription;

    // Texto livre, pode ser nulo -- leva por exemplo o responsibleEmployeeId órfão.
    private String detail;
}
