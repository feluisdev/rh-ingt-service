package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetDepartamentosQuery implements Query {

  @NotBlank(message = "The field <tamanho> is required.")
  private String tamanho;
  @NotBlank(message = "The field <pagina> is required.")
  private String pagina;
  @NotBlank(message = "The field <nome> is required.")
  private String nome;
  @NotBlank(message = "The field <localizacao> is required.")
  private String localizacao;
  @NotBlank(message = "The field <codigo> is required.")
  private String codigo;
  @NotBlank(message = "The field <responsavelId> is required.")
  private String responsavelId;
  @NotBlank(message = "The field <estado> is required.")
  private String estado;

}