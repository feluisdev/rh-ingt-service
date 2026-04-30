package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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


  private LocalDateTime createdAt ;


  private LocalDateTime updatedAt ;

}
