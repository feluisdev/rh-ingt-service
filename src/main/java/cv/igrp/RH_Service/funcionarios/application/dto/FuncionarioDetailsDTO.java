/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.DependenteResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.QualificacaoResponseDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class FuncionarioDetailsDTO {

  
  
  private String funcionarioId ;
  
  
  private String numSegurado ;
  
  
  private String nome ;
  
  
  private String nif ;
  
  
  private String nib ;
  
  
  private String email ;
  
  
  private String sexo ;
  
  
  private String endereco ;
  
  
  private String estadoCivil ;
  
  
  private String estado ;
  
  
  private String estadoDesc ;
  
  
  private LocalDate createdAt ;
  
  @Valid
  private List<DocumentoResponseDTO> anexos = new ArrayList<>();
  
  @Valid
  private ContratoResponseDTO contratoAtual ;
  
  @Valid
  private QualificacaoResponseDTO qualificacoes ;
  
  @Valid
  private DependenteResponseDTO dependentes ;

}