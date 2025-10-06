package cv.igrp.RH_Service.options.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExistsByCcodeAndCkeyQuery implements Query {

  @NotBlank(message = "The field <ccode> is required")
  private String ccode;
  @NotBlank(message = "The field <ckey> is required")
  private String ckey;

}