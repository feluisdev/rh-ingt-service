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

  @NotBlank(message = "O campo <mission> é obrigatório")
  
  private String mission ;
  @NotBlank(message = "O campo <vision> é obrigatório")
  
  private String vision ;
  
  
  private List<String> values = new ArrayList<>();
  
  
  private String versionComment ;

}