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

  @NotBlank(message = "The field <documentoId> is required.")
  private String documentoId;
  @NotBlank(message = "The field <idTipoDocumento> is required.")
  private String idTipoDocumento;
  @NotBlank(message = "The field <estado> is required.")
  private String estado;
  @NotBlank(message = "The field <pagina> is required.")
  private String pagina;
  @NotBlank(message = "The field <tamanho> is required.")
  private String tamanho;

}