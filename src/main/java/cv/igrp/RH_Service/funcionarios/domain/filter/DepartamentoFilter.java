package cv.igrp.RH_Service.funcionarios.domain.filter;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DepartamentoFilter {

  private String nome;
  private String localizacao;
  private String codigo;
  private Estado estado;
  private ExternalID responsavelId;
  private Integer pageNumber;
  private Integer pageSize;
}
