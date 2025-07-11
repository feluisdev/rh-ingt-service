package cv.igrp.RH_Service.contratos.application.queries.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetContratoQuery implements Query {

  @NotBlank(message = "The field <tipo_contrato> is required.")
  private String tipo_contrato;

}