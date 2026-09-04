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

  @NotBlank(message = "O campo <classifier> é obrigatório")
  private String classifier;

  @NotBlank(message = "O campo <organicUnitId> é obrigatório")
  private String organicUnitId;

  @NotNull(message = "O campo <fiscalYear> é obrigatório")
  private Integer fiscalYear;

  private String requestedAmount;
}
