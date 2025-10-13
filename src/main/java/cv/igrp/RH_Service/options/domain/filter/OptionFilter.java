package cv.igrp.RH_Service.options.domain.filter;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OptionFilter {

  private String ccode;
  private String ckey;
  private String cvalue;
  private String locale;
  private Integer sortOrder;
  private boolean active;
  private Integer pageNumber;
  private Integer pageSize;
}
