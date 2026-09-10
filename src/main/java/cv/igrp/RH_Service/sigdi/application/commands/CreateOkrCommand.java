package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import cv.igrp.RH_Service.sigdi.application.dto.CreateOkrDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOkrCommand implements Command {

  // A-135-03 (mesma família, achada ao construir o portão do plano 07): este objeto nunca é
  // parâmetro @Valid @RequestBody de nenhum controlador -- é construído dentro do método do
  // controlador (TaticalController.createOkr: new CreateOkrCommand(dto)), fora do alcance de
  // qualquer validação de bean do Spring. As restrições que aqui estiveram nunca cascatearam para
  // CreateOkrDTO; a validação real do corpo já acontece no parâmetro
  // @Valid @RequestBody CreateOkrDTO createOkrRequest do próprio controlador, antes deste
  // objeto ser construído -- remover a anotação daqui não remove garantia nenhuma. O
  // SpringCommandBus não corre Bean Validation sobre objetos Command, só faz dispatch por
  // classe. Remoção segue o precedente 613207ed (Fase 135, UpdateTacticalActivityCommand) e
  // SIA-06 (Fase 113).
  private CreateOkrDTO data;

}
