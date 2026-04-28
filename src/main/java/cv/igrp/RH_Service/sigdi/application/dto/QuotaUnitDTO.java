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
public class QuotaUnitDTO  {



  private String organicUnitId ;


  private String organicUnitName ;


  private Integer totalCollaborators ;


  private Integer excellentAllowed ;


  private Integer excellentAssigned ;


  private Integer goodAllowed ;


  private Integer goodAssigned ;


  private Boolean isCompliant ;


  private List<QuotaViolationDTO> violations = new ArrayList<>();

}
