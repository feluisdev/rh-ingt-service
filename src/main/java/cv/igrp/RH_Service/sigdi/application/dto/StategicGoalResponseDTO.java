/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.sigdi.application.dto.ChildGoalsDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class StategicGoalResponseDTO  {

  
  
  private UUID id ;
  
  
  private UUID identityId ;
  
  
  private String perspective ;
  
  
  private String perspectiveDesc ;
  
  
  private BigDecimal weight ;
  
  
  private String title ;
  
  
  private String description ;
  
  @Valid
  private List<ChildGoalsDTO> childGoals = new ArrayList<>();
  
  
  private UUID parentGoalId ;
  
  
  private Integer linkedActivities ;
  
  
  private Double progress ;
  
  
  private String status ;

  private String statusDesc ;

  private Integer year ;

  private List<StrategicIndicatorDTO> indicators = new ArrayList<>();

}