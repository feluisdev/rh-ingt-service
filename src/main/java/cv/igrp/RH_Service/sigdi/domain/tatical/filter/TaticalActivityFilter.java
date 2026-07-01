package cv.igrp.RH_Service.sigdi.domain.tatical.filter;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaticalActivityFilter {

  private final Integer pageNumber;
  private final Integer pageSize;
  private final String paaLevel;
}

