package cv.igrp.RH_Service.funcionarios.domain.filter;


import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DependenteFilter {

  private ExternalID funcionarioExternalId;
  private String nome;
  private Integer pageNumber;
  private Integer pageSize;
}
