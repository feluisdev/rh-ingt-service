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


  private Integer linkedActivities ;
  
  
  private Double progress ;
  
  
  private String status ;

  private String statusDesc ;

  private Integer year ;

  private List<StrategicIndicatorDTO> indicators = new ArrayList<>();

  // FIX-09 / A-124-02: the links that the rule in force would no longer allow to be created,
  // produced by StrategyLinkCoherencePolicy AFTER the goal is saved. Decision 2 of
  // 130-CONTEXT.md is WARN AND SAVE: this list travels on a 200 next to the saved goal, and it is
  // never an error payload -- routing it through the error path would make the product look like
  // it failed when it saved, which is the mirror image of the defect being fixed.
  //
  // NO INITIALIZER, unlike "indicators" above, and the difference is deliberate: this field must
  // stay null when there is nothing to warn about, so that "no warning" is a state of its own
  // rather than an empty list a caller has to interpret. A regeneration by iGRP Studio would
  // delete this field and the warning would stop reaching the user without anything failing --
  // UpdateStategicGoalDtoPerspectiveContractTest fails in that case.
  private List<IncoherentLinkDTO> incoherentLinks;

}