package cv.igrp.RH_Service.shared.application.dto;

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
public class PageDTO {

  
  
  private Integer pageNumber ;
  
  
  private Integer pageSize ;
  
  
  private Long totalElements ;
  
  
  private Integer totalPages ;
  
  
  private boolean last ;
  
  
  private boolean first ;

}