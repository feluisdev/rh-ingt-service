package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoRequestDTO;
import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTipoDocumentoQuery implements Query {


  private TipoDocumentoRequestDTO getTipoDocumento;
  @NotBlank(message = "The field <id> is required")
  private String id;

}
