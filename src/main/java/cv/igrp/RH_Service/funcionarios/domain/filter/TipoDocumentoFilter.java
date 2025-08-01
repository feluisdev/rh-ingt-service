package cv.igrp.RH_Service.funcionarios.domain.filter;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;
@Builder
@Getter
public class TipoDocumentoFilter {

  private String descricao ;
  private String codigo ;
  private Estado estado ;
  private Integer pageNumber;
  private Integer pageSize;
}
