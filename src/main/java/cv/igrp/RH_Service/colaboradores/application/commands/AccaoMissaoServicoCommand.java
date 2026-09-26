package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.MissaoServicoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** PEDIR, AUTORIZAR, RECUSAR, REGRESSO, CANCELAR uma missão de serviço (comoMe: em /me). */
@Getter
@RequiredArgsConstructor
public class AccaoMissaoServicoCommand implements Command {
    private final String missaoId;
    private final String accao;
    private final MissaoServicoRequestDTO request;
    private final boolean comoMe;
}
