package cv.igrp.RH_Service.contratos.application.dto;

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
public class ContratoResponseDTO {

  
  
  private Integer cargo_id;
  
  
  private String tipo_contrato;
  
  
  private String data_inicio;
  
  
  private String data_fim;
  
  
  private String salario;
  
  
  private String carga_horaria;
  
  
  private String status;
  
  
  private String observacoes;
  
  
  private String created_at;
  
  
  private String updated_at;

}