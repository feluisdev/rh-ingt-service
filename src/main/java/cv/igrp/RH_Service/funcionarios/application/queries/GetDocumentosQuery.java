package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetDocumentosQuery implements Query {

  @NotBlank(message = "The field <external_id> is required.")
  private String external_id;
  @NotBlank(message = "The field <url> is required.")
  private String url;
  @NotBlank(message = "The field <observacao> is required.")
  private String observacao;
  @NotBlank(message = "The field <object_id> is required.")
  private String object_id;
  @NotBlank(message = "The field <estado> is required.")
  private String estado;

}