package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.SaldoAusenciaRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateSaldoAusenciaCommand implements Command {
    private final String funcionarioId;
    private final SaldoAusenciaRequestDTO request;
}
