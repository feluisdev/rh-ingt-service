package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.DadosBancariosService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsCreateDadosBancariosCommandHandler")
@RequiredArgsConstructor
public class CreateDadosBancariosCommandHandler
        implements CommandHandler<CreateDadosBancariosCommand, ResponseEntity<Map<String, ?>>> {

    private final DadosBancariosService dadosBancariosService;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateDadosBancariosCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        var saved = dadosBancariosService.criarDadosBancarios(funcionarioId, command.getRequest());
        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
