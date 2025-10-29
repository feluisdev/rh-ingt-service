/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class QualificacaoResponseDTO {

  
  
  private String qualificacaoId ;
  
  
  private String instituicao ;
  
  
  private String curso ;
  
  
  private LocalDate dataInicio ;
  
  
  private LocalDate dataConclusao ;
  
  
  private String nivel ;
  
  
  private String situacao ;
  
  
  private Integer cargaHoraria ;
  
  
  private BigDecimal notaFinal ;
  
  @Valid
  private DocumentoResponseDTO anexo ;
  
  
  private String estado ;
  
  
  private String estadoDesc ;

}