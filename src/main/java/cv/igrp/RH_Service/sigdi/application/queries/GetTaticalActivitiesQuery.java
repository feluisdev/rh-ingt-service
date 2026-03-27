package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTaticalActivitiesQuery implements Query {

  @NotBlank(message = "The field <pageNumber> is required")
  private String pageNumber;
  @NotBlank(message = "The field <pageSize> is required")
  private String pageSize;
  @NotBlank(message = "The field <status> is required")
  private String status;
  @NotBlank(message = "The field <unidade> is required")
  private String unidade;
  @NotBlank(message = "The field <data> is required")
  private String data;

}