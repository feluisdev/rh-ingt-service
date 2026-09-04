/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

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
public class CreateDelegationRequestDTO  {

  @NotBlank(message = "O campo <delegateUserId> é obrigatório")

  private String delegateUserId ;
  @NotBlank(message = "O campo <scope> é obrigatório")

  private String scope ;
  @NotBlank(message = "O campo <startDate> é obrigatório")

  private String startDate ;
  @NotBlank(message = "O campo <endDate> é obrigatório")

  private String endDate ;


  private String reason ;

}
