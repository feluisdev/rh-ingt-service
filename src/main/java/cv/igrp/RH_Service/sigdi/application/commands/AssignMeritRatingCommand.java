package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.AssignMeritRatingRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignMeritRatingCommand implements Command {

    // A-135-03 (mesma família, achada ao construir o portão do plano 07): este objeto nunca é
    // parâmetro @Valid @RequestBody de nenhum controlador -- é construído dentro do método do
    // controlador (ComplianceController.assignMeritRating:
    // commandBus.send(new AssignMeritRatingCommand(id, body))), fora do alcance de qualquer
    // validação de bean do Spring. O campo é transporte interno preenchido pelo controlador a
    // partir do {id} do caminho; a fonte de verdade é o caminho, não o corpo. O
    // SpringCommandBus não corre Bean Validation sobre objetos Command, só faz dispatch por
    // classe -- a restrição de presença obrigatória que aqui esteve nunca foi avaliada.
    // Remoção segue o precedente 613207ed
    // (Fase 135, UpdateTacticalActivityCommand) e SIA-06 (Fase 113).
    private String evaluationId;

    // Mesma razão do campo acima: a validação real do corpo já acontece no parâmetro
    // @Valid @RequestBody AssignMeritRatingRequestDTO body do próprio controlador, antes deste
    // objeto ser construído -- as restrições que aqui estiveram nunca cascatearam.
    private AssignMeritRatingRequestDTO body;
}
