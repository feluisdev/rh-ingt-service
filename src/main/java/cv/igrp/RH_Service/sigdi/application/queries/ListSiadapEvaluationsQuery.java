package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListSiadapEvaluationsQuery implements Query {

  @NotNull(message = "O campo <year> é obrigatório")
  private Integer year;

  private String organicUnitId;
  private String status;
  private String pageNumber;
  private String pageSize;
  private String evaluatorId;

  public ListSiadapEvaluationsQuery(Integer year, String organicUnitId, String status, String pageNumber, String pageSize) {
    this(year, organicUnitId, status, pageNumber, pageSize, null);
  }
}

