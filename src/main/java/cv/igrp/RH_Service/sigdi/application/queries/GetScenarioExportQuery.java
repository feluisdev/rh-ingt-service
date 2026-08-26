package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetScenarioExportQuery implements Query {

  @NotBlank(message = "O campo <scenarioId> é obrigatório")
  private String scenarioId;

  private String format;
}
