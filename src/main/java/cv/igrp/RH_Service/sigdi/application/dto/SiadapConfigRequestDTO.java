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

  @NotNull(message = "O campo <goodScore> é obrigatório")

  private BigDecimal goodScore ;
  @NotNull(message = "O campo <excellentScore> é obrigatório")

  private BigDecimal excellentScore ;
  @NotNull(message = "O campo <excellentQuota> é obrigatório")

  private BigDecimal excellentQuota ;
  @NotNull(message = "O campo <goodQuota> é obrigatório")

  private BigDecimal goodQuota ;
  @NotNull(message = "O campo <minimumCollaboratorsForQuota> é obrigatório")

  private Integer minimumCollaboratorsForQuota ;
  @NotNull(message = "O campo <resultsWeight> é obrigatório")

  private BigDecimal resultsWeight ;
  @NotNull(message = "O campo <competenciesWeight> é obrigatório")

  private BigDecimal competenciesWeight ;

}
