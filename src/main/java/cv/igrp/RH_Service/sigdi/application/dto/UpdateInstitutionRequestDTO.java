package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class UpdateInstitutionRequestDTO {

  @NotBlank(message = "The field <name> is required")
  private String name;

  @NotBlank(message = "The field <type> is required")
  private String type;

  private String contactEmail;
}
