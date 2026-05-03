package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.LicencaMobilidadeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateLicencaMobilidadeCommand implements Command {
    private final String funcionarioId;
    private final String licencaId;
    private final LicencaMobilidadeRequestDTO request;
}
