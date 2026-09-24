package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.FeriasPreferenciaRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** O próprio indica a preferência de férias do ano ({@code /me}, art. 5.º n.º 4 do DL n.º 3/2010). */
@Getter
@RequiredArgsConstructor
public class IndicarMinhaPreferenciaFeriasCommand implements Command {
    private final int ano;
    private final FeriasPreferenciaRequestDTO request;
}
