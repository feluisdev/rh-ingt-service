package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ContractTypeResponseDTO {
    private String id;
    private String code;
    private String description;
    private String vinculoLaboralId;
    private Boolean isRenewable;
    private Integer maxRenewals;
    private Integer maxDurationMonths;
    private Boolean isActive;
}
