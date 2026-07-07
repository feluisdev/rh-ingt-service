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
public class RecordObjectiveAchievementRequestDTO {

    @NotBlank(message = "evaluationId is mandatory")
    private String evaluationId;

    @NotBlank(message = "objectiveCode is mandatory")
    private String objectiveCode;

    @NotNull(message = "achievedValue is mandatory")
    private BigDecimal achievedValue;

    @NotNull(message = "score (1/3/5) is mandatory")
    private Integer score;
}
