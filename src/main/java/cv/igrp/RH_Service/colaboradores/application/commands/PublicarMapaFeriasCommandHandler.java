package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.MapaFeriasService;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Dar conhecimento do mapa de férias — DL n.º 3/2010, art. 6.º n.º 1. */
@Component
@RequiredArgsConstructor
public class PublicarMapaFeriasCommandHandler
        implements CommandHandler<PublicarMapaFeriasCommand, ResponseEntity<SuccessResponseDTO>> {

    private final MapaFeriasService mapaFeriasService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(PublicarMapaFeriasCommand command) {
        var publicacao = mapaFeriasService.publicar(command.getAno());
        return ResponseEntity.status(201).body(new SuccessResponseDTO(
                publicacao.mapa().getId().getStringValor(), true, new ArrayList<>(publicacao.alertas())));
    }
}
