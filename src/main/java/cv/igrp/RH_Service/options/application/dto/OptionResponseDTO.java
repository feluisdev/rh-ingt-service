/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.options.application.dto;

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
public class OptionResponseDTO  {

  
  
  private String id ;
  
  
  private String ccode ;
  
  
  private String ckey ;
  
  
  private String cvalue ;
  
  
  private String locale ;
  
  
  private Integer sort_order ;
  
  
  private String description ;

}