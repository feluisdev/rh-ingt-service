package cv.igrp.RH_Service.funcionarios.domain.filter;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FuncionarioFilter {

  private String nome;
  private String nif;
  private String numSegurado;
  private String email;
  private Integer pageNumber;
  private Integer pageSize;


}
