package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.PageDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor

@EqualsAndHashCode(callSuper = true)
@IgrpDTO
public class WrapperListaOptionDTO extends PageDTO {


  @Valid
  private List<OptionResponseDTO> content = new ArrayList<>();

}
