package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DecisaoMarcacaoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DecidirMarcacaoCommand implements Command {
    private final boolean pelaChefia;
    private final String funcionarioId;
    private final String marcacaoId;
    private final boolean validar;
    private final DecisaoMarcacaoRequestDTO request;
}
