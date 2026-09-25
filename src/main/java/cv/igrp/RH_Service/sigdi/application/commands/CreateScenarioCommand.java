package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.CreateScenarioRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScenarioCommand implements Command {

  // A-135-03 (mesma família, achada ao construir o portão do plano 07): este objeto nunca é
  // parâmetro @Valid @RequestBody de nenhum controlador -- é construído dentro do método do
  // controlador (IntelligenceController.createScenario: new
  // CreateScenarioCommand(createScenarioRequest)), fora do alcance de qualquer validação de
  // bean do Spring, logo as restrições que aqui estiveram nunca cascatearam. A validação real do corpo
  // já acontece no parâmetro @Valid @RequestBody CreateScenarioRequestDTO do próprio
  // controlador, antes deste objeto ser construído. O SpringCommandBus não corre Bean
  // Validation sobre objetos Command, só faz dispatch por classe. Remoção segue o precedente
  // 613207ed (Fase 135, UpdateTacticalActivityCommand) e SIA-06 (Fase 113).
  private CreateScenarioRequestDTO body;
}
