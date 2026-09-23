package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateHorarioCommand implements Command {
    private final HorarioRequestDTO horarioRequest;
    private final String horarioId;
}
