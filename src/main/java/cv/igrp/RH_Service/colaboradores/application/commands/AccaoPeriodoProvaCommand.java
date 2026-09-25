package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoProvaRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Um passo do período de prova: RELATORIO (o tutor, /me), CONCLUIR, CESSAR ou DENUNCIAR. */
@Getter
@RequiredArgsConstructor
public class AccaoPeriodoProvaCommand implements Command {
    private final String funcionarioId;
    private final String periodoId;
    private final String accao;
    private final PeriodoProvaRequestDTO request;
}
