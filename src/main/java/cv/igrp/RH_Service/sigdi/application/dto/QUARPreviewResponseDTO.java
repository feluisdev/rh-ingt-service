/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class QUARPreviewResponseDTO  {



  private String institutionId ;


  private String institutionName ;


  private Integer year ;


  private String generatedAt ;


  private QUARCompletenessDTO completeness ;


  private List<QUARObjectiveDTO> objectives = new ArrayList<>();


  private QUARBudgetSummaryDTO budgetSummary ;

}
