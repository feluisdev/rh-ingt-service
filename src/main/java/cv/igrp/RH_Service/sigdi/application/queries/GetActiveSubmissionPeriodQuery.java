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

    @NotBlank(message = "O campo <type> é obrigatório")
    private String type; // UNIT_LEVEL | INDIVIDUAL_LEVEL

    private String purpose; // PAA | SIADAP — optional, defaults to PAA in the handler

    // Optional (see 59-REVIEW.md WR-03): when present, scopes the active-period lookup to
    // this fiscal year (mirroring ContractualizeObjectivesCommandHandler's year-scoped gate).
    // When absent, existing callers keep the year-agnostic, date-range-only lookup.
    private Integer year;
}
