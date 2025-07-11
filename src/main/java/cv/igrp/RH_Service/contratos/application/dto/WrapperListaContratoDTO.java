package cv.igrp.RH_Service.contratos.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.contratos.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.PageDTO;
@Data
@NoArgsConstructor
@AllArgsConstructor

@IgrpDTO
public class WrapperListaContratoDTO extends PageDTO{

  
  @Valid
  private ContratoResponseDTO content;

}