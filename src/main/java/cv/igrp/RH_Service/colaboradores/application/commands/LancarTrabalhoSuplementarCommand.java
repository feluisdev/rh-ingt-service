package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Lançar trabalho suplementar, já autorizado: pelo RH ({@code funcionarioId} do caminho) ou pela chefia directa. */
@Getter
@RequiredArgsConstructor
public class LancarTrabalhoSuplementarCommand implements Command {
    private final boolean pelaChefia;
    private final String funcionarioId;
    private final TrabalhoSuplementarRequestDTO request;
}
