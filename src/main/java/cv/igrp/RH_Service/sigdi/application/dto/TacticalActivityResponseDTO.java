/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import lombok.EqualsAndHashCode;
@Data
@NoArgsConstructor
@AllArgsConstructor

@EqualsAndHashCode(callSuper = true)
@IgrpDTO
public class TacticalActivityResponseDTO extends CreateTacticalActivityDTO {

  
  
  private UUID id ;
  
  
  private Integer version ;
  
  
  private String status ;
  
  
  private String statusDesc ;

  private String organicUnitName ;

  private String responsibleName ;

}