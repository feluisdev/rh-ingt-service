package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.shared.application.constants.GrauParentesco;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class DependenteRequestDTO {

  
  
  private String nome ;
  
  
  private GrauParentesco parentesco ;
  
  
  private LocalDate dataNascimento ;
  
  
  private String cpf ;

}