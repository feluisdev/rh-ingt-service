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

  @NotBlank(message = "O campo <name> é obrigatório")
  private String name;

  @NotBlank(message = "O campo <type> é obrigatório")
  private String type;

  private String contactEmail;
}
