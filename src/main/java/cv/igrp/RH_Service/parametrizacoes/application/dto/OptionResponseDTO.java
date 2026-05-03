package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class OptionResponseDTO {



  private String optionId ;


  private String ccode ;


  private String ckey ;


  private String cvalue ;


  private String locale ;


  private Integer sortOrder ;


  private boolean active ;


  private String description ;

}
