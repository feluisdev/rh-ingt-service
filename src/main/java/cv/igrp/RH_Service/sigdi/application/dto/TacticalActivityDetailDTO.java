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
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class TacticalActivityDetailDTO  {



  private String id ;


  private String strategicGoalId ;


  private String organicUnitId ;

  private String organicUnitName ;


  private String title ;


  private String justificationWhy ;


  private String responsibleWho ;

  private String responsibleName ;


  private String locationWhere ;


  private String methodologyHow ;


  private String startDate ;


  private String endDate ;


  private Integer fiscalYear ;


  private String economicClassifier ;


  private BigDecimal budgetEstimated ;


  private BigDecimal budgetCommitted ;


  private BigDecimal budgetLiquidated ;


  private BigDecimal budgetPaid ;


  private String status ;


  private String statusDesc ;


  private Integer version ;


  private String createdAt ;


  private String updatedAt ;


  private List<WorkflowHistoryItemDTO> workflowHistory = new ArrayList<>();


  private List<ChangeRequestResponseDTO> changeRequests = new ArrayList<>();

}