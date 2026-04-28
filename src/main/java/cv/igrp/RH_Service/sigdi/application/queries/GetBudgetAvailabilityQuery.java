package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetBudgetAvailabilityQuery implements Query {

  @NotBlank(message = "The field <classifier> is required")
  private String classifier;

  @NotBlank(message = "The field <organicUnitId> is required")
  private String organicUnitId;

  @NotNull(message = "The field <fiscalYear> is required")
  private Integer fiscalYear;

  private String requestedAmount;
}
