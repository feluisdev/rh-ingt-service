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
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class OkrResponseDTO  {



  private String id ;


  private String institutionId ;


  private String strategicGoalId ;


  private String title ;


  private String cycle ;


  private String status ;


  private BigDecimal progress ;


  private String createdAt ;


  private List<OkrKeyResultResponseDTO> keyResults = new ArrayList<>();

}