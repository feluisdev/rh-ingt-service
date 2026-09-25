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
public class UpdateStategicGoalDTO  {

  @Size(min = 5, message = "O campo <title> deve ter pelo menos 5 caracteres")
	@Size(max = 100, message = "O campo <title> não pode ter mais de 100 caracteres")
  
  private String title ;
  @Size(min = 1, message = "O campo <description> deve ter pelo menos 1 caractere")
	@Size(max = 500, message = "O campo <description> não pode ter mais de 500 caracteres")
  
  private String description ;
  // Editado manualmente apesar do cabeçalho "DO NOT MODIFY" -- mesmo precedente do commit
  // 613207ed (A-135-2AA); a D-52 mediu que este Update tem o mesmo buraco do Create
  // (CreateStategicGoalDTO.java), não só o Create fichado em A-135-02. Cliente:
  // GoalSchema.weight, src/app/(myapp)/types/strategy.ts:191-194, intervalo [0.1, 10]. 136-09.
  //
  // LIMITAÇÃO DO MANIFESTO: o tipo "decimal" de .igrpstudio/sigdi/dto/UpdateStategicGoalDTO.json
  // só tem a chave "positive" -- não tem "min"/"max". @DecimalMin/@DecimalMax NÃO PODEM ser
  // declarados no manifesto, e uma regeneração do IGRP Studio deita estas anotações fora sem que
  // nada falhe (136-MANIFESTOS.md, secção c). O guarda é
  // StrategicGoalWeightValidationTest#updateWeight* -- se a regeneração apagar estas
  // anotações, é esse teste que fica vermelho, não o manifesto.
  @DecimalMin(value = "0.1", message = "O campo <weight> não pode ser inferior a 0.1")
  @DecimalMax(value = "10", message = "O campo <weight> não pode ser superior a 10")
  private BigDecimal weight ;

  @Min(value = 2000, message = "O campo <year> não pode ser inferior a 2000")
  @Max(value = 2100, message = "O campo <year> não pode ser superior a 2100")
  private Integer year ;

  // FIX-09 / A-124-02: the BSC perspective code, as a String. Until Phase 130 this field did not
  // exist at all: the client sent "perspective", Jackson had nowhere to put it, the service does
  // not configure fail-on-unknown-properties, and the value was dropped in silence while the
  // response said 200 and the toast said the changes had been saved.
  //
  // The absence of an initializer is deliberate, for the same reason as "indicators" below: it is
  // what lets Jackson tell an ABSENT key (field stays null, meaning "keep the current
  // perspective") apart from a value the caller actually sent. A regeneration by iGRP Studio
  // would delete this field outright and reopen A-124-02 --
  // UpdateStategicGoalDtoPerspectiveContractTest fails in that case, turning the regeneration
  // into a red build instead of silent data loss.
  private String perspective ;

  // The absence of an initializer is deliberate: it is what lets Jackson tell an ABSENT
  // "indicators" key (field stays null) apart from an EMPTY list (field is a non-null, empty
  // list). Absent means "do not touch the existing indicators"; [] means "remove them all".
  // A regeneration by iGRP Studio would put the empty-ArrayList initializer back on this field
  // and, with it, the silent data loss of finding A-124-01 --
  // UpdateStategicGoalDtoIndicatorsContractTest fails in that case.
  private java.util.List<StrategicIndicatorDTO> indicators;

}