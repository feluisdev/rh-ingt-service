/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class CostDriverRequestDTO  {

  
  
  private String driverType ;
  @NotNull(message = "The field <parameters> is required")
  
  private Map<String, ?> parameters = new HashMap<>();
  @NotNull(message = "The field <valid_from> is required")
  
  private LocalDate valid_from ;
  
  
  private LocalDate valid_until ;

}