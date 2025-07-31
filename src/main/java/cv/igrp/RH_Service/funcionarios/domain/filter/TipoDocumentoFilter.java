package cv.igrp.RH_Service.funcionarios.domain.filter;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;
@Builder
@Getter

public class TipoDocumentoFilter {
  private Integer id ;
  private UUID external_id ;
  private String descricao ;
  private String codigo ;
}
