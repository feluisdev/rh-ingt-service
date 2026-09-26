package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.JuntaMedicaRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** PEDIR, PARECER ou CANCELAR a junta médica. */
@Getter
@RequiredArgsConstructor
public class AccaoJuntaMedicaCommand implements Command {
    private final String funcionarioId;
    private final String juntaId;
    private final String accao;
    private final JuntaMedicaRequestDTO request;
}
