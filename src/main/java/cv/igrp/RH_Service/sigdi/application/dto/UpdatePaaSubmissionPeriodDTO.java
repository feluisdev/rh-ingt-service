package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 133-03 / JAN-03 (D-18, D-19): deliberately no purpose/type field -- altering WHICH window this
// is would not be an alteration, it would be creating a different window
// (PaaSubmissionPeriod.changeSchedule javadoc, 133-UI-SPEC.md Appendix B).
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class UpdatePaaSubmissionPeriodDTO {

    @NotNull(message = "O campo <startDate> é obrigatório")
    private LocalDate startDate;

    @NotNull(message = "O campo <endDate> é obrigatório")
    private LocalDate endDate;

    @NotNull(message = "O campo <year> é obrigatório")
    private Integer year;
}
