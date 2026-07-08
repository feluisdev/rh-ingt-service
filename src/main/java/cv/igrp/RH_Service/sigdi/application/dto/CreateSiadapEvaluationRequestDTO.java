package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class CreateSiadapEvaluationRequestDTO {

    @NotBlank(message = "employeeId is mandatory")
    private String employeeId;

    @NotNull(message = "year is mandatory")
    private Integer year;

    private String organicUnitId;
    private String evaluatorId;
    private BigDecimal resultsWeight;
    private BigDecimal competenciesWeight;
}
