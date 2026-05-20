package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.FuncionarioService;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCreateFuncionarioCommandHandler")
@RequiredArgsConstructor
public class CreateFuncionarioCommandHandler
        implements CommandHandler<CreateFuncionarioCommand, ResponseEntity<Map<String, ?>>> {

    private final FuncionarioService funcionarioService;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateFuncionarioCommand command) {
        var saved = funcionarioService.criarFuncionario(command.getRequest());
        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "numeroFuncionario", saved.getNumeroFuncionario(),
                "message", "Criado com sucesso"));
    }
}
