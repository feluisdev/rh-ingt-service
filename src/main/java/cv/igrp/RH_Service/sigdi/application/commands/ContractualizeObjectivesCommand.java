package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.ContractualizeObjectivesRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractualizeObjectivesCommand implements Command {

    // A-135-03 (mesma família, achada ao construir o portão do plano 07): este objeto nunca é
    // parâmetro @Valid @RequestBody de nenhum controlador -- é construído dentro do método do
    // controlador (ComplianceController.contractualizeObjectives: new
    // ContractualizeObjectivesCommand(request)), fora do alcance de qualquer validação de bean
    // do Spring, logo as restrições que aqui estiveram nunca cascatearam. A validação real do corpo já
    // acontece no parâmetro @Valid @RequestBody List<IndividualObjectiveDTO> objectives do
    // próprio controlador, antes deste objeto ser construído. O SpringCommandBus não corre
    // Bean Validation sobre objetos Command, só faz dispatch por classe. Remoção segue o
    // precedente 613207ed (Fase 135, UpdateTacticalActivityCommand) e SIA-06 (Fase 113).
    private ContractualizeObjectivesRequestDTO body;
}
