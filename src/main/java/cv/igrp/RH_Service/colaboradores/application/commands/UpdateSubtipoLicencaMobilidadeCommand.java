package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.SubtipoLicencaMobilidadeRequest;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateSubtipoLicencaMobilidadeCommand implements Command {
    private final SubtipoLicencaMobilidadeRequest request;
    private final String id;
}
