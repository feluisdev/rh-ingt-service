package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.sigdi.application.dto.CreateTacticalActivityDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTacticalActivityCommand implements Command {

  private CreateTacticalActivityDTO tacticalActivity;

  // Mesma família do A-135-2AA (2026-09-09): este objeto é o próprio parâmetro
  // @Valid @RequestBody de TaticalController.updateTacticalActivity, que só chama
  // setId(id) DEPOIS de o Spring já ter corrido a validação de bean na vinculação do
  // pedido. Um @NotBlank aqui só "funcionava" porque o único cliente real
  // (functions/tactical.ts:updateActivity) sempre envia id no corpo — coincidência do
  // payload, não garantia do servidor; qualquer chamador direto do Gateway que seguisse
  // o contrato documentado (id vem do caminho) levava sempre 400. Removido pelo mesmo
  // precedente do RecordObjectiveAchievementRequestDTO/SubmitSelfEvaluationRequestDTO
  // (Fase 113, SIA-06): o campo é transporte interno preenchido pelo controlador, a
  // fonte de verdade é o {id} do caminho, não o corpo.
  private String id;

}
