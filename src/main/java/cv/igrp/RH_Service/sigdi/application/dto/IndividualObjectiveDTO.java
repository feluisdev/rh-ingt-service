package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class IndividualObjectiveDTO {
    private String code;
    private String description;
    private String indicator;
    private BigDecimal targetValue;
    private BigDecimal achievedValue;
    private Integer score;
    private BigDecimal weight;
}
