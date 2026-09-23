package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.FeriasPreferenciaRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IndicarPreferenciaFeriasCommand implements Command {
    private final String funcionarioId;
    private final int ano;
    private final FeriasPreferenciaRequestDTO request;
}
