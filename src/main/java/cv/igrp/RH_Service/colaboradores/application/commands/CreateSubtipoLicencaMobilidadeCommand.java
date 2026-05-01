package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.SubtipoLicencaMobilidadeRequest;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateSubtipoLicencaMobilidadeCommand implements Command {
    private final SubtipoLicencaMobilidadeRequest request;
}
