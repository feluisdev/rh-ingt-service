/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.shared.application.constants.Estado;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class TipoDocumentoRequestDTO  {

  
  
  private Integer id ;
  
  
  private String external_id ;
  
  
  private String descrcicao ;
  
  
  private String codigo ;
  
  
  private Estado Estado ;

}