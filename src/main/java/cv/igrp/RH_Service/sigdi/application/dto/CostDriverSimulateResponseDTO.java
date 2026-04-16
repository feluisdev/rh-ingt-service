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
import java.util.HashMap;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class CostDriverSimulateResponseDTO  {



  private String driverType ;


  private BigDecimal unitCost ;


  private String currency ;


  private BigDecimal totalCost ;


  private String driverVersion ;


  private String driverSource ;


  private String calculatedAt ;


  private Map<String, Object> params = new HashMap<>();


  private Map<String, Object> breakdown = new HashMap<>();

}