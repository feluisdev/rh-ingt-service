package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.HorarioColaboradorRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AtribuirHorarioColaboradorCommand implements Command {
    private final String funcionarioId;
    private final HorarioColaboradorRequestDTO request;
}
