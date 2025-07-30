package cv.igrp.RH_Service.funcionarios.domain.filter;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class DocumentoFilter {
  private Integer id;
  private UUID externalId;
  private String url;
  private String observacao;
  private String objectoTipo;
  private UUID objectId;
  private Estado estado;
}
