package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ParametroFeriasRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateParametroFeriasCommand implements Command {
    private final ParametroFeriasRequestDTO parametroFeriasRequest;
    private final String parametroFeriasId;
}
