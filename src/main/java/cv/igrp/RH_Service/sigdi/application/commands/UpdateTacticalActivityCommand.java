package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.sigdi.application.dto.CreateTacticalActivityDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTacticalActivityCommand implements Command {

  // D-52 (Fase 136, plano 09): faltava @Valid aqui. Este objeto É o parâmetro
  // @Valid @RequestBody de TaticalController.updateTacticalActivity (ver
  // SigdiCommandNoBeanValidationTest, que documenta esta classe como a única exceção
  // legítima do módulo a essa posição) -- mas o @Valid do controlador só valida os
  // campos declarados diretamente NESTE comando (id, abaixo). A validação de bean só
  // cascata para um campo embrulhado quando esse campo tem o seu próprio @Valid --
  // sem ele, o Spring nunca desce a CreateTacticalActivityDTO, e a alteração de
  // atividade não tinha validação de campo nenhuma no servidor, nem sequer o
  // @Size(min = 1) fraco do caminho de criação. Confirmado por leitura direta a
  // 2026-09-10. Com este @Valid, o @Size(min = 10) de justificationWhy (136-09,
  // Task 1) e as restantes restrições de CreateTacticalActivityDTO passam a valer
  // também na alteração, não só na criação -- ver
  // UpdateTacticalActivityCommandValidationTest#nestedInvalidJustificationWhyViolatesConstraint.
  @Valid
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
