package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ItemChecklistRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Marcar um item; sem funcionarioId, é o próprio ou a chefia em /me. */
@Getter
@RequiredArgsConstructor
public class MarcarItemChecklistCommand implements Command {
    private final String funcionarioId;
    private final String checklistId;
    private final String itemId;
    private final ItemChecklistRequestDTO request;
}
