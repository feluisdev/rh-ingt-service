package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ContractualizeObjectivesRequestDTO {

    @NotNull(message = "evaluationId is mandatory")
    private String evaluationId;

    @NotEmpty(message = "objectives list cannot be empty")
    private List<IndividualObjectiveDTO> objectives;
}
