/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.HashMap;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class CostDriverSimulateReqDTO  {

  @NotBlank(message = "O campo <driverType> é obrigatório")

  private String driverType ;

  @NotNull(message = "O campo <params> é obrigatório")

  private Map<String, Object> params = new HashMap<>();

}