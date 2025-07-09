package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.shared.application.constants.EstadoCivil;
import cv.igrp.RH_Service.shared.application.constants.Sexo;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class FuncionarioRequestDTO {

  
  
  private String numSegurado ;
  
  
  private String nome ;
  
  
  private String nif ;
  
  
  private String nib ;
  @Email(message = "Invalid email format for field <email>.")
  
  private String email ;
  
  
  private Sexo sexo ;
  
  
  private String endereco ;
  
  
  private EstadoCivil estadoCivil ;

}