package cv.igrp.RH_Service.funcionarios.domain.filter;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class CargoFilter {

  private String nome;
  private BigDecimal salarioBaseMin;
  private BigDecimal salarioBaseMax;
  private Integer nivelHierarquico;
  private Estado estado;
  private Integer pageNumber;
  private Integer pageSize;
}
