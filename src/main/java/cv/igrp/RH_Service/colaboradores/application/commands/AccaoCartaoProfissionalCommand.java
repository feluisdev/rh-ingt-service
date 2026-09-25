package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.CartaoProfissionalRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Entregar (ENTREGAR), devolver (DEVOLVER) ou anular (ANULAR) um cartão. */
@Getter
@RequiredArgsConstructor
public class AccaoCartaoProfissionalCommand implements Command {
    private final String funcionarioId;
    private final String cartaoId;
    private final String accao;
    private final CartaoProfissionalRequestDTO request;
}
