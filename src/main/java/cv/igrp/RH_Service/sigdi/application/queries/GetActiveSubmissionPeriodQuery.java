package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetActiveSubmissionPeriodQuery implements Query {

    @NotBlank(message = "The field <type> is required")
    private String type; // UNIT_LEVEL | INDIVIDUAL_LEVEL
}
