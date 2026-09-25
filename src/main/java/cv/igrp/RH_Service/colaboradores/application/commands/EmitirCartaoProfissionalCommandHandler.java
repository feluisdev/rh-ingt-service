package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.CartaoProfissionalDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.CartoesDtos;
import cv.igrp.RH_Service.colaboradores.application.services.CartaoProfissionalService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmitirCartaoProfissionalCommandHandler implements CommandHandler<EmitirCartaoProfissionalCommand, ResponseEntity<CartaoProfissionalDTO>> {

    private final CartaoProfissionalService service;

    @IgrpCommandHandler
    public ResponseEntity<CartaoProfissionalDTO> handle(EmitirCartaoProfissionalCommand c) {
        var f = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        return ResponseEntity.status(201).body(CartoesDtos.dto(service.emitir(f), null));
    }
}
