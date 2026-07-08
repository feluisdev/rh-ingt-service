package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class CreatePaaSubmissionPeriodDTO {

    @NotNull(message = "The field <type> is required")
    private String type; // UNIT_LEVEL | INDIVIDUAL_LEVEL

    @NotNull(message = "The field <startDate> is required")
    private LocalDate startDate;

    @NotNull(message = "The field <endDate> is required")
    private LocalDate endDate;

    @NotNull(message = "The field <year> is required")
    private Integer year;

    private String purpose; // PAA | SIADAP — optional, defaults to PAA in the handler
}
