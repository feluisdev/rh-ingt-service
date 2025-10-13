package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindByCcodeQuery implements Query {

  @NotBlank(message = "The field <locale> is required")
  private String locale;
  @NotBlank(message = "The field <includeInactive> is required")
  private String includeInactive;
  @NotBlank(message = "The field <format> is required")
  private String format;
  @NotBlank(message = "The field <ccode> is required")
  private String ccode;

}