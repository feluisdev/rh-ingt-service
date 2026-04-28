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

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class BudgetAvailabilityResponseDTO  {



  private String classifier ;


  private String classifierDescription ;


  private String organicUnitId ;


  private String organicUnitName ;


  private Integer fiscalYear ;


  private BigDecimal budgetAllocated ;


  private BigDecimal committed ;


  private BigDecimal liquidated ;


  private BigDecimal paid ;


  private BigDecimal available ;


  private BigDecimal executionRate ;


  private String alertLevel ;


  private String dataSource ;


  private String lastSyncAt ;


  private Boolean requestedAmountFeasible ;


  private BigDecimal requestedAmount ;

}