package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class ContratoResponseDTO {

  
  
  private String contratoId ;
  
  
  private String funcionarioId ;
  
  
  private String departamentoId ;
  
  
  private String cargoId ;
  
  
  private String tipoContrato ;
  
  
  private String tipoContratoDesc ;
  
  
  private LocalDate dataInicio ;
  
  
  private LocalDate dataFim ;
  
  
  private BigDecimal salario ;
  
  
  private Integer cargaHoraria ;
  
  
  private String observacoes ;
  
  
  private String estado ;
  
  
  private String estadoDesc ;

}