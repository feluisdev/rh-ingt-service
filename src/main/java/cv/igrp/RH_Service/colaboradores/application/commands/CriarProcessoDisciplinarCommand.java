package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.CriarProcessoDisciplinarRequest;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CriarProcessoDisciplinarCommand implements Command {
    private final String funcionarioId;
    private final CriarProcessoDisciplinarRequest request;
}
