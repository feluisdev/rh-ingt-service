package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Abrir uma checklist de entrada ou de saída à mão. */
@Getter
@RequiredArgsConstructor
public class AbrirChecklistCommand implements Command {
    private final String funcionarioId;
    private final ChecklistRequestDTO request;
}
