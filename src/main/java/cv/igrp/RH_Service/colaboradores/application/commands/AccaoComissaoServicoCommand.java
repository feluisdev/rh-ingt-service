package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ComissaoServicoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** RENOVAR ou CESSAR a comissão de serviço. */
@Getter
@RequiredArgsConstructor
public class AccaoComissaoServicoCommand implements Command {
    private final String funcionarioId;
    private final String licencaId;
    private final String accao;
    private final ComissaoServicoRequestDTO request;
}
