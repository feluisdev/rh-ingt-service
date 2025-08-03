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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class FuncionarioResponseDTO  {

  
  
  private String funcionarioId ;
  
  
  private String numSegurado ;
  
  
  private String nome ;
  
  
  private String nif ;
  
  
  private String nib ;
  
  
  private String email ;
  
  
  private String sexo ;
  
  
  private String endereco ;
  
  
  private String departamento ;
  
  
  private String cargo ;
  
  
  private String estadoCivil ;
  
  
  private String estado ;
  
  
  private String estadoDesc ;
  
  
  private LocalDate createdAt ;
  
  
  private LocalDate updatedAt ;
  
  @Valid
  private List<DocumentoResponseDTO> anexos = new ArrayList<>();

}