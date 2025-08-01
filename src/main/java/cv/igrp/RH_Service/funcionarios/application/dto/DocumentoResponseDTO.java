/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class DocumentoResponseDTO {

  
  
  private String documentoId ;
  
  
  private String url ;
  
  
  private String observacao ;
  
  
  private String idTipoDocumento ;
  
  
  private String tipoDocumento ;
  
  
  private String estado ;
  
  
  private String estadoDesc ;

}