package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MarcacaoAssiduidadeId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnularMarcacaoCommandHandler implements CommandHandler<AnularMarcacaoCommand, ResponseEntity<SuccessResponseDTO>> {

    private final AssiduidadeService assiduidadeService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(AnularMarcacaoCommand command) {
        String motivo = command.getRequest() != null ? command.getRequest().getMotivo() : null;
        var m = assiduidadeService.anular(FuncionarioId.from(command.getFuncionarioId()),
                MarcacaoAssiduidadeId.from(command.getMarcacaoId()), motivo);
        return ResponseEntity.ok(SuccessResponseDTO.de(m.getId().getStringValor()));
    }
}
