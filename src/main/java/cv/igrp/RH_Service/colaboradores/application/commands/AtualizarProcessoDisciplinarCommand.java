package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AtualizarProcessoDisciplinarRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AtualizarProcessoDisciplinarCommand implements Command {
    private final String funcionarioId;
    private final String processoId;
    private final AtualizarProcessoDisciplinarRequestDTO request;
}
