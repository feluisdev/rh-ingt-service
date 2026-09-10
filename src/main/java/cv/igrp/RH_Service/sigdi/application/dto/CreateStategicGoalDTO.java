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
public class CreateStategicGoalDTO  {

  @Size(min = 5, message = "O campo <title> deve ter pelo menos 5 caracteres")
	@Size(max = 100, message = "O campo <title> não pode ter mais de 100 caracteres")
  
  private String title ;
  @NotBlank(message = "O campo <perspective> é obrigatório")
  
  private String perspective ;
  @Size(min = 1, message = "O campo <description> deve ter pelo menos 1 caractere")
	@Size(max = 500, message = "O campo <description> não pode ter mais de 500 caracteres")
  
  private String description ;
  // Editado manualmente apesar do cabeçalho "DO NOT MODIFY" -- mesmo precedente do commit
  // 613207ed (A-135-2AA): o cliente já exige o intervalo [0.1, 10] (GoalSchema.weight,
  // src/app/(myapp)/types/strategy.ts:191-194) e o servidor nunca o validou. 136-09, D-52.
  //
  // LIMITAÇÃO DO MANIFESTO: o tipo "decimal" de .igrpstudio/sigdi/dto/CreateStategicGoalDTO.json
  // só tem a chave "positive" -- não tem "min"/"max". @DecimalMin/@DecimalMax NÃO PODEM ser
  // declarados no manifesto, e uma regeneração do IGRP Studio deita estas anotações fora sem que
  // nada falhe (136-MANIFESTOS.md, secção c). O guarda é
  // StrategicGoalWeightValidationTest#createWeight* -- se a regeneração apagar estas
  // anotações, é esse teste que fica vermelho, não o manifesto.
  @DecimalMin(value = "0.1", message = "O campo <weight> não pode ser inferior a 0.1")
  @DecimalMax(value = "10", message = "O campo <weight> não pode ser superior a 10")
  private BigDecimal weight ;

  @Min(value = 2000, message = "O campo <year> não pode ser inferior a 2000")
  @Max(value = 2100, message = "O campo <year> não pode ser superior a 2100")
  private Integer year ;

  private java.util.List<StrategicIndicatorDTO> indicators = new java.util.ArrayList<>();

}