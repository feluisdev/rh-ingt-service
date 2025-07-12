package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class FuncionarioResponseDTO {

  
  
  private String externalID ;
  
  
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
  
  
  private LocalDate updatedAt ;

}