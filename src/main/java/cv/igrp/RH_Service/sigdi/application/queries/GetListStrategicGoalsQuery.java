package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetListStrategicGoalsQuery implements Query {

  @NotBlank(message = "O campo <perspective> é obrigatório")
  private String perspective;
  @NotBlank(message = "O campo <status> é obrigatório")
  private String status;
  @NotBlank(message = "O campo <pageNumber> é obrigatório")
  private String pageNumber;
  @NotBlank(message = "O campo <pageSize> é obrigatório")
  private String pageSize;

}