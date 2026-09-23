package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** O próprio pede trabalho suplementar ({@code /me}). */
@Getter
@RequiredArgsConstructor
public class PedirTrabalhoSuplementarCommand implements Command {
    private final TrabalhoSuplementarRequestDTO request;
}
