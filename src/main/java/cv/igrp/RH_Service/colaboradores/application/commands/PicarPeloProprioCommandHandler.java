package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Picagem em tempo real pelo próprio: só em teletrabalho ou regime misto (Lei n.º 20/X/2023, art. 170.º). */
@Component
@RequiredArgsConstructor
public class PicarPeloProprioCommandHandler implements CommandHandler<PicarPeloProprioCommand, ResponseEntity<SuccessResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final AssiduidadeService assiduidadeService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(PicarPeloProprioCommand command) {
        var sentido = LancarMarcacaoCommandHandler.sentido(command.getRequest() != null ? command.getRequest().getSentido() : null);
        var r = assiduidadeService.picarPeloProprio(currentEmployeeResolver.resolve(), sentido);
        return ResponseEntity.status(201).body(SuccessResponseDTO.de(r.marcacao().getId().getStringValor(),
                r.alertas().toArray(String[]::new)));
    }
}
