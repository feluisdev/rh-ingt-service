package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetQUARExportQuery implements Query {

  @NotNull(message = "O campo <year> é obrigatório")
  private Integer year;

  private String format;
  private String organicUnitId;
  private Boolean force;
}
