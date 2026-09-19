package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.FuncionarioService;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.colaboradores.application.dto.FuncionarioCriadoResponseDTO;

@Component("colabsCreateFuncionarioCommandHandler")
@RequiredArgsConstructor
public class CreateFuncionarioCommandHandler
        implements CommandHandler<CreateFuncionarioCommand, ResponseEntity<FuncionarioCriadoResponseDTO>> {

    private final FuncionarioService funcionarioService;

    @IgrpCommandHandler
    public ResponseEntity<FuncionarioCriadoResponseDTO> handle(CreateFuncionarioCommand command) {
        var saved = funcionarioService.criarFuncionario(command.getRequest());
        return ResponseEntity.status(201).body(new FuncionarioCriadoResponseDTO(
                saved.getId().getStringValor(),
                saved.getNumeroFuncionario()));
    }
}
