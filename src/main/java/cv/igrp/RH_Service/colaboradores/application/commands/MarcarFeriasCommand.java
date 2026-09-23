package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.FeriasMarcacaoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MarcarFeriasCommand implements Command {
    private final String funcionarioId;
    private final int ano;
    private final FeriasMarcacaoRequestDTO request;
}
