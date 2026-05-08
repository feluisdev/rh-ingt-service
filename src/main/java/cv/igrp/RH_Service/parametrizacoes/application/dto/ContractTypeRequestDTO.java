package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ContractTypeRequestDTO {
    private String code;
    private String description;
    private String professionalSituationId;
    private Boolean isRenewable;
    private Integer maxRenewals;
    private Integer maxDurationMonths;
}
