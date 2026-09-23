package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService;
import cv.igrp.RH_Service.colaboradores.domain.models.SentidoMarcacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LancarMarcacaoCommandHandler implements CommandHandler<LancarMarcacaoCommand, ResponseEntity<SuccessResponseDTO>> {

    private final AssiduidadeService assiduidadeService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(LancarMarcacaoCommand command) {
        var dto = command.getRequest();
        var r = assiduidadeService.lancar(FuncionarioId.from(command.getFuncionarioId()), dto.getMomento(),
                sentido(dto.getSentido()), dto.getMotivo());
        return ResponseEntity.status(201).body(SuccessResponseDTO.de(r.marcacao().getId().getStringValor(),
                r.alertas().toArray(String[]::new)));
    }

    static SentidoMarcacao sentido(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return SentidoMarcacao.valueOf(valor.trim());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "sentido inválido: '" + valor + "'. Valores: ENTRADA, SAIDA.");
        }
    }
}
