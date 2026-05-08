package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class DesativarDocumentoSubRecursoCommand implements Command {
    private final String referenceEntity;
    private final UUID referenceId;
    private final String documentoId;
}
