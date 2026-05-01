package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.LicencaMobilidadeRequest;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateLicencaMobilidadeCommand implements Command {
    private final String funcionarioId;
    private final LicencaMobilidadeRequest request;
}
