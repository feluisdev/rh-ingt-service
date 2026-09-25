package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloseEvaluationsCommand implements Command {

  // A-135-03 (mesma família, achada ao construir o portão do plano 07): este objeto nunca é
  // parâmetro @Valid @RequestBody de nenhum controlador -- é construído dentro do método do
  // controlador (ComplianceController.closeEvaluations: new
  // CloseEvaluationsCommand(closeEvaluationsRequest)), fora do alcance de qualquer validação
  // de bean do Spring, logo as restrições que aqui estiveram nunca cascatearam. A validação real do
  // corpo já acontece no parâmetro @Valid @RequestBody CloseEvaluationsRequestDTO do próprio
  // controlador, antes deste objeto ser construído. O SpringCommandBus não corre Bean
  // Validation sobre objetos Command, só faz dispatch por classe. Remoção segue o precedente
  // 613207ed (Fase 135, UpdateTacticalActivityCommand) e SIA-06 (Fase 113).
  private CloseEvaluationsRequestDTO body;
}
