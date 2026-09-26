package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** ACRESCENTAR um item ou CANCELAR a checklist. */
@Getter
@RequiredArgsConstructor
public class AccaoChecklistCommand implements Command {
    private final String funcionarioId;
    private final String checklistId;
    private final String accao;
    private final ChecklistRequestDTO request;
}
