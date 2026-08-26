// Mantido à mão, não gerado pelo iGRP Studio: DTO de saída do EligibleResponsiblesResolver
// (Fase 116), sem manifesto correspondente em .igrpstudio/sigdi/dto/. Ver 116-03-PLAN.md.
package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class EligibleUnitGroupDTO {

    private String unitId;

    private String unitName;

    private String unitAcronym;

    private List<EligibleResponsibleDTO> responsibles = new ArrayList<>();
}
