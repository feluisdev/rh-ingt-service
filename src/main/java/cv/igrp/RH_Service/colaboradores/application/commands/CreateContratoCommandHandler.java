package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.ContratoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCreateContratoCommandHandler")
@RequiredArgsConstructor
public class CreateContratoCommandHandler
        implements CommandHandler<CreateContratoCommand, ResponseEntity<Map<String, ?>>> {

    private final ContratoService contratoService;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateContratoCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        var saved = contratoService.criarContrato(funcionarioId, command.getRequest());
        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
