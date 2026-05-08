package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class RegistarDocumentoContratoCommand implements Command {
    private final String contratoId;
    private final UUID documentTypeId;
    private final String fileKey;
    private final String originalFilename;
    private final String contentType;
    private final long fileSize;
    private final String description;
}
