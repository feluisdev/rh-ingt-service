package cv.igrp.RH_Service.parametrizacoes.application.dto;

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
public class OptionRequestDTO {



  @NotBlank
  private String ccode ;


  @NotBlank
  private String ckey ;


  @NotBlank
  private String cvalue ;


  private String locale ;


  private Integer sortOrder ;


  private String description ;

}
