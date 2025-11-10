package cv.igrp.RH_Service.funcionarios.domain.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Builder
@Getter
@With
public class OptionFilter {
  private String ccode ;
  private String ckey ;
  private String cvalue ;
  private String locale ;
  private Integer sort_order ;
  private boolean active ;
  private String description ;

  private Integer pageNumber;
  private Integer pageSize;
}
