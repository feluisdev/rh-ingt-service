package cv.igrp.RH_Service.options.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetListOptionsQuery implements Query {

  @NotBlank(message = "The field <ccode> is required")
  private String ccode;
  @NotBlank(message = "The field <ckey> is required")
  private String ckey;
  @NotBlank(message = "The field <cvalue> is required")
  private String cvalue;
  @NotBlank(message = "The field <locale> is required")
  private String locale;
  @NotNull(message = "The field <sortOrder> is required")
  private Integer sortOrder;
  @NotNull(message = "The field <active> is required")
  private boolean active;
  @NotBlank(message = "The field <pageNumber> is required")
  private String pageNumber;
  @NotBlank(message = "The field <pageSize> is required")
  private String pageSize;

}