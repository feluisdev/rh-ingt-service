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
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class CreateTacticalActivityDTO  {

  @NotNull(message = "O campo <strategicGoalId> é obrigatório")
  
  private UUID strategicGoalId ;
  @NotNull(message = "O campo <organicUnitId> é obrigatório")
  
  private UUID organicUnitId ;
  @NotBlank(message = "O campo <title> é obrigatório")
	@Size(min = 5, message = "O campo <title> deve ter pelo menos 5 caracteres")
	@Size(max = 200, message = "O campo <title> não pode ter mais de 200 caracteres")
  
  private String title ;
  @Size(min = 1, message = "O campo <descriptionWhat> deve ter pelo menos 1 caractere")
	@Size(max = 2000, message = "O campo <descriptionWhat> não pode ter mais de 2000 caracteres")
  
  private String descriptionWhat ;
  @Size(min = 1, message = "O campo <justificationWhy> deve ter pelo menos 1 caractere")
	@Size(max = 2000, message = "O campo <justificationWhy> não pode ter mais de 2000 caracteres")
  
  private String justificationWhy ;
  @Size(min = 1, message = "O campo <locationWhere> deve ter pelo menos 1 caractere")
	@Size(max = 2000, message = "O campo <locationWhere> não pode ter mais de 2000 caracteres")
  
  private String locationWhere ;
  private UUID responsibleWho ;
  @Size(min = 1, message = "O campo <methodologyHow> deve ter pelo menos 1 caractere")
	@Size(max = 2000, message = "O campo <methodologyHow> não pode ter mais de 2000 caracteres")
  
  private String methodologyHow ;
  @NotNull(message = "O campo <startDate> é obrigatório")
  
  private LocalDate startDate ;
  @NotNull(message = "O campo <endDate> é obrigatório")
  
  private LocalDate endDate ;
  private BigDecimal budgetEstimated ;
  private String economicClassifier ;

  // PAA Level: UNIT_LEVEL (default) or INDIVIDUAL_LEVEL
  private String paaLevel;

}