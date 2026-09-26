package cv.igrp.RH_Service.formacao.application.commands;

import cv.igrp.RH_Service.formacao.application.dto.PlanoFormacaoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** CRIAR o plano, NECESSIDADE, APROVAR. */
@Getter
@RequiredArgsConstructor
public class AccaoPlanoFormacaoCommand implements Command {
    private final String planoId;
    private final String accao;
    private final PlanoFormacaoRequestDTO request;
    private final boolean comoMe;
}
