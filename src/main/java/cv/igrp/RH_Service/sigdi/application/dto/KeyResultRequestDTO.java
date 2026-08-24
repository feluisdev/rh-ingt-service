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
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class KeyResultRequestDTO  {

  @NotBlank(message = "O campo <title> é obrigatório")
  
  private String title ;
  @NotNull(message = "O campo <targetValue> é obrigatório")
  
  private BigDecimal targetValue ;
  
  
  private BigDecimal currentValue ;
  
  
  private String metricUnit ;
  @NotNull(message = "O campo <activityId> é obrigatório")
  
  private UUID activityId ;

  private BigDecimal criteriaSuperado;
  private BigDecimal criteriaSegurancaMin;
  private BigDecimal criteriaSegurancaMax;
  private BigDecimal criteriaAlcancadoMin;
  private BigDecimal criteriaAlcancadoMax;
  private BigDecimal criteriaInsuficiente;

  private BigDecimal weight;
  private UUID okrId;

}