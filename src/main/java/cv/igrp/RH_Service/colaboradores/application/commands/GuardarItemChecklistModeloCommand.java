package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ItemChecklistModeloRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Criar (sem itemId) ou alterar um item do modelo. */
@Getter
@RequiredArgsConstructor
public class GuardarItemChecklistModeloCommand implements Command {
    private final String itemId;
    private final ItemChecklistModeloRequestDTO request;
}
