package cv.igrp.RH_Service.funcionarios.domain.filter;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class DocumentoFilter {

  private String nomeFuncionario;
  private ExternalID idFuncionario;
  private ExternalID documentoId;
  private ExternalID idTipoDocumento;
  private Estado estado;

  private Integer pageNumber;
  private Integer pageSize;
}
