/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* EXTENDED WITH CUSTOM FIELDS FOR SIADAP REFACTORING */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class SiadapEvaluationDTO  {

  private String id ;
  private String employeeId ;
  private String employeeName ;
  private String organicUnitId ;
  private String organicUnitName ;
  private String evaluatorId ;
  private String evaluatorName ;
  private String year ;
  private BigDecimal objectivesScore ;
  private BigDecimal competenciesScore ;
  private BigDecimal finalScore ;
  private String meritRating ;
  private Boolean quotaValidated ;
  private String status ;
  private String lastUpdatedAt ;

  // Rich evaluation fields
  private String phase;
  private String acceptanceStatus;
  private String acceptanceStatusDesc;
  // WR-02: last negotiation comment/justification submitted by the avaliado, so the avaliador
  // can see why negotiation was requested. Single last-comment field — no history (out of scope).
  private String lastNegotiationComment;
  private BigDecimal selfEvaluationScore;
  private BigDecimal resultsWeight;
  private BigDecimal competenciesWeight;
  private List<IndividualObjectiveDTO> objectives;
  private List<CompetencyItemDTO> competencies;

}
