/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

// MANUALLY EDITED — Phase 92 (OKR-01): fixed a field-name typo on the value field
// (it was missing its "e") and removed the unintended required-evidence constraint on
// evidenceUrl, which the domain has always treated as optional. Regenerating this file
// via IGRP Studio will reintroduce both defects.

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class KeyResultCheckinRequestDTO  {

  @NotNull(message = "The field <valueAdded> is required")

  private BigDecimal valueAdded ;
  @NotBlank(message = "The field <comment> is required")

  private String comment ;

  private String evidenceUrl ;

}