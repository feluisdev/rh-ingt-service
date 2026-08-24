/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* EXTENDED WITH CUSTOM FIELDS — DO NOT REVERT WITHOUT UPDATING THE iGRP STUDIO MANIFEST */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class CloseEvaluationsRequestDTO  {

  @NotNull(message = "O campo <year> é obrigatório")

  private Integer year ;

  // organicUnitId is intentionally optional (NOT @NotBlank): null/blank means "close every
  // unit's evaluations for the year" (preserves prior default behaviour); a non-blank value
  // scopes the close to one organic unit via findByYearAndOrganicUnitId. See 81-CONTEXT.md
  // Open Question 1 (resolved) and T-81-01 in 81-01-PLAN.md's threat model.
  private String organicUnitId ;

}
