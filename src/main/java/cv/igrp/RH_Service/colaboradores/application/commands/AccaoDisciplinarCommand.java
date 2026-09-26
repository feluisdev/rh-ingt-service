package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AccaoDisciplinarRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Um acto da tramitação do processo disciplinar (PARTICIPAR sem processoId). */
@Getter
@RequiredArgsConstructor
public class AccaoDisciplinarCommand implements Command {
    private final String funcionarioId;
    private final String processoId;
    private final String accao;
    private final AccaoDisciplinarRequestDTO request;
}
