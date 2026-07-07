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
public class SubmitSelfEvaluationRequestDTO {

    @NotBlank(message = "evaluationId is mandatory")
    private String evaluationId;

    @NotNull(message = "selfEvaluationScore is mandatory")
    private BigDecimal selfEvaluationScore;
}
