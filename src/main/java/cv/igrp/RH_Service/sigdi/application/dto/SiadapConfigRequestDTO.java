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

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class SiadapConfigRequestDTO  {

  @NotNull(message = "The field <goodScore> is required")

  private BigDecimal goodScore ;
  @NotNull(message = "The field <excellentScore> is required")

  private BigDecimal excellentScore ;
  @NotNull(message = "The field <excellentQuota> is required")

  private BigDecimal excellentQuota ;
  @NotNull(message = "The field <goodQuota> is required")

  private BigDecimal goodQuota ;
  @NotNull(message = "The field <minimumCollaboratorsForQuota> is required")

  private Integer minimumCollaboratorsForQuota ;
  @NotNull(message = "The field <resultsWeight> is required")

  private BigDecimal resultsWeight ;
  @NotNull(message = "The field <competenciesWeight> is required")

  private BigDecimal competenciesWeight ;

}
