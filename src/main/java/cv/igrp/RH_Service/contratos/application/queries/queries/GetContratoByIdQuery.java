package cv.igrp.RH_Service.contratos.application.queries.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetContratoByIdQuery implements Query {

  @NotNull(message = "The field <contratoId> is required.")
  private Integer contratoId;

}