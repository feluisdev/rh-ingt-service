/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class QUARObjectiveDTO  {



  private String id ;


  private String title ;


  private String perspective ;


  private BigDecimal weight ;


  private BigDecimal planned ;


  private BigDecimal realized ;


  private BigDecimal deviation ;


  private BigDecimal deviationPct ;


  private String rating ;


  private List<QUARKeyResultDTO> keyResults = new ArrayList<>();

}
