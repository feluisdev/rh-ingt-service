package cv.igrp.RH_Service.contratos.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor

@IgrpDTO
public class ContratoRequestDTO {

  
  
  private String tipo_contrato;
  
  
  private LocalDateTime data_inicio;
  
  
  private LocalDateTime data_fim;
  
  
  private BigDecimal salario;
  
  
  private Integer carga_horaria;
  
  
  private String status;
  
  
  private String observacoes;
  
  
  private LocalDateTime created_at;
  
  
  private LocalDateTime updated_at;

}