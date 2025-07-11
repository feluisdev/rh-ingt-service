package cv.igrp.RH_Service.contratos.application.queries.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.contratos.application.dto.ContratoRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContratoQuery implements Query {

  
  private ContratoRequestDTO contratorequest;

}