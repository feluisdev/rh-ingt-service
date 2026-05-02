package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RejeitarLicencaMobilidadeCommand implements Command {
    private final String licencaId;
    private final String rejectionReason;
}
