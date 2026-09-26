package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AcumulacaoFuncoesRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** PEDIR, AUTORIZAR, INDEFERIR ou CESSAR a acumulação de funções (comoMe: o próprio em /me). */
@Getter
@RequiredArgsConstructor
public class AccaoAcumulacaoFuncoesCommand implements Command {
    private final String funcionarioId;
    private final String acumulacaoId;
    private final String accao;
    private final AcumulacaoFuncoesRequestDTO request;
    private final boolean comoMe;
}
