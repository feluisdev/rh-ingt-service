package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetFuncionariosQuery implements Query {

  @NotBlank(message = "The field <nome> is required")
  private String nome;
  @NotBlank(message = "The field <email> is required")
  private String email;
  @NotBlank(message = "The field <numSegurado> is required")
  private String numSegurado;
  @NotBlank(message = "The field <nif> is required")
  private String nif;
  @NotBlank(message = "The field <pagina> is required")
  private String pagina;
  @NotBlank(message = "The field <tamanho> is required")
  private String tamanho;

}