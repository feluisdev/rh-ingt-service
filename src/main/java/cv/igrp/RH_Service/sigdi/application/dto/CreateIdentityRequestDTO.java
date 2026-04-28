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
public class CreateIdentityRequestDTO  {

  @NotBlank(message = "The field <mission> is required")
  
  private String mission ;
  @NotBlank(message = "The field <vision> is required")
  
  private String vision ;
  
  
  private List<String> values = new ArrayList<>();
  
  
  private String versionComment ;

}