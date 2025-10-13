package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCargosQuery implements Query {

  @NotBlank(message = "The field <nome> is required")
  private String nome;
  @NotBlank(message = "The field <codigo> is required")
  private String codigo;
  @NotNull(message = "The field <nivelHierarquico> is required")
  private Integer nivelHierarquico;
  @NotNull(message = "The field <salarioBaseMax> is required")
  private Integer salarioBaseMax;
  @NotNull(message = "The field <salarioBaseMin> is required")
  private Integer salarioBaseMin;
  @NotBlank(message = "The field <estado> is required")
  private String estado;
  @NotBlank(message = "The field <pagina> is required")
  private String pagina;
  @NotBlank(message = "The field <tamanho> is required")
  private String tamanho;

}