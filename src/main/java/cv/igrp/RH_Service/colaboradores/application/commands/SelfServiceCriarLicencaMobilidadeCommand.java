package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.SelfServiceCriarLicencaMobilidadeRequest;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SelfServiceCriarLicencaMobilidadeCommand implements Command {
    private final FuncionarioId funcionarioId;
    private final SelfServiceCriarLicencaMobilidadeRequest request;
}
