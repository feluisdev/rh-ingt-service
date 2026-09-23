package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DecisaoTrabalhoSuplementarRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CancelarTrabalhoSuplementarCommand implements Command {
    private final String funcionarioId;
    private final String trabalhoId;
    private final DecisaoTrabalhoSuplementarRequestDTO request;
}
