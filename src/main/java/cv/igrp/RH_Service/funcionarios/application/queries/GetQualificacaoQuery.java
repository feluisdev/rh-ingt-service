package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetQualificacaoQuery implements Query {

  @NotBlank(message = "The field <funcionarioId> is required.")
  private String funcionarioId;
  @NotBlank(message = "The field <qualificacaoId> is required.")
  private String qualificacaoId;

}