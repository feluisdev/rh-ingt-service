package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllSubmissionPeriodsQuery implements Query {
    private String pageNumber;
    private String pageSize;
    private String purpose; // PAA | SIADAP — optional; when absent, list is unfiltered by purpose
}
