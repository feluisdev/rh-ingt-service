package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "O campo <type> é obrigatório")
    private String type; // UNIT_LEVEL | INDIVIDUAL_LEVEL

    @NotNull(message = "O campo <startDate> é obrigatório")
    private LocalDate startDate;

    @NotNull(message = "O campo <endDate> é obrigatório")
    private LocalDate endDate;

    @NotNull(message = "O campo <year> é obrigatório")
    private Integer year;

    @NotBlank(message = "O campo <purpose> é obrigatório")
    private String purpose; // PAA_BSC_OBJECTIVES | PAA | SIADAP | SIADAP_INTERIM | SIADAP_SELF_EVAL | SIADAP_FINAL
}
