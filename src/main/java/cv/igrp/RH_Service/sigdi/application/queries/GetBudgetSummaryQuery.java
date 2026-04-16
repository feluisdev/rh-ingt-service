package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetBudgetSummaryQuery implements Query {

  private String fiscalYear;
  private String organicUnitId;
}
