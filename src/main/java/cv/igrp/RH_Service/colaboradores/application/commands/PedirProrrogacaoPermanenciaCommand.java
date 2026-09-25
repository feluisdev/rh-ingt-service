package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProrrogacaoPermanenciaRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Pedir a permanência ao serviço para além dos 65 anos (art. 48.º n.os 2 e 3). */
@Getter
@RequiredArgsConstructor
public class PedirProrrogacaoPermanenciaCommand implements Command {
    private final String funcionarioId;
    private final ProrrogacaoPermanenciaRequestDTO request;
}
