package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.TrabalhoSuplementarService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TrabalhoSuplementarId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelarTrabalhoSuplementarCommandHandler
        implements CommandHandler<CancelarTrabalhoSuplementarCommand, ResponseEntity<SuccessResponseDTO>> {

    private final TrabalhoSuplementarService trabalhoSuplementarService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CancelarTrabalhoSuplementarCommand command) {
        String motivo = command.getRequest() != null ? command.getRequest().getMotivo() : null;
        var t = trabalhoSuplementarService.cancelar(FuncionarioId.from(command.getFuncionarioId()),
                TrabalhoSuplementarId.from(command.getTrabalhoId()), motivo);
        return ResponseEntity.ok(SuccessResponseDTO.de(t.getId().getStringValor()));
    }
}
