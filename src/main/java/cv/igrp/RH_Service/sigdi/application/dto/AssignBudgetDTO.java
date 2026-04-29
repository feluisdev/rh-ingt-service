package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AssignBudgetDTO {

    @NotNull(message = "Activity ID is required")
    private UUID activityId;

    @NotNull(message = "Budget estimated is required")
    private BigDecimal budgetEstimated;

    @NotBlank(message = "Economic classifier is required")
    private String economicClassifier;
}
