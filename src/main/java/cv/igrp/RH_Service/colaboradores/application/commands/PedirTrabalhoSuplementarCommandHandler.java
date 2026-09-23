package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.TrabalhoSuplementarService;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static cv.igrp.RH_Service.colaboradores.application.commands.LancarTrabalhoSuplementarCommandHandler.hora;
import static cv.igrp.RH_Service.colaboradores.application.commands.LancarTrabalhoSuplementarCommandHandler.pedido;

@Component
@RequiredArgsConstructor
public class PedirTrabalhoSuplementarCommandHandler
        implements CommandHandler<PedirTrabalhoSuplementarCommand, ResponseEntity<SuccessResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final TrabalhoSuplementarService trabalhoSuplementarService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(PedirTrabalhoSuplementarCommand command) {
        var dto = pedido(command.getRequest());
        var t = trabalhoSuplementarService.pedir(currentEmployeeResolver.resolve(), dto.getData(),
                hora(dto.getHoraInicio(), "horaInicio"), hora(dto.getHoraFim(), "horaFim"), dto.getMotivo());
        return ResponseEntity.status(201).body(SuccessResponseDTO.de(t.getId().getStringValor()));
    }
}
