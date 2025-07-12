package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class CargoResponseDTO {

  
  
  private String cargoId ;
  
  
  private String nome ;
  
  
  private String descricao ;
  
  
  private String codigo ;
  
  
  private BigDecimal salarioBase ;
  
  
  private Integer nivelHierarquico ;
  
  
  private String estado ;
  
  
  private String estadoDesc ;

}