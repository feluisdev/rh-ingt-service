package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.DadosBancariosService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("colabsCreateDadosBancariosCommandHandler")
@RequiredArgsConstructor
public class CreateDadosBancariosCommandHandler
        implements CommandHandler<CreateDadosBancariosCommand, ResponseEntity<SuccessResponseDTO>> {

    private final DadosBancariosService dadosBancariosService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreateDadosBancariosCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        var saved = dadosBancariosService.criarDadosBancarios(funcionarioId, command.getRequest());
        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
