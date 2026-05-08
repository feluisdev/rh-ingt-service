package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class RegistarDocumentoLicencaCommand implements Command {
    private final String funcionarioId;
    private final String licencaId;
    private final UUID documentTypeId;
    private final String fileKey;
    private final String originalFilename;
    private final String contentType;
    private final long fileSize;
    private final String description;
}
