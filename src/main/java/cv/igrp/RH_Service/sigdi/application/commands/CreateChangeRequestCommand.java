package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChangeRequestCommand implements Command {

  private ChangeRequestDTO changerequest;

  // A-135-03 (Fase 136, plano 07): este objeto nunca é parâmetro @Valid @RequestBody de
  // nenhum controlador -- é construído dentro do método do controlador
  // (TaticalController.createChangeRequest: new CreateChangeRequestCommand(dto,
  // activityId)), fora do alcance de qualquer validação de bean do Spring. O campo é
  // transporte interno preenchido pelo controlador a partir do {activityId} do caminho; a
  // fonte de verdade é o caminho, não o corpo. O SpringCommandBus não corre Bean Validation
  // sobre objetos Command, só faz dispatch por classe -- a restrição de presença obrigatória
  // que aqui esteve nunca foi avaliada. Remoção segue o precedente 613207ed (Fase 135, UpdateTacticalActivityCommand)
  // e SIA-06 (Fase 113).
  private String activityId;
}
