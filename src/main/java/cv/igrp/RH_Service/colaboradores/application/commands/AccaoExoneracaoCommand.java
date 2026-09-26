package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ExoneracaoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** PEDIR, DEFERIR ou DESISTIR da exoneração voluntária (comoMe: o próprio em /me). */
@Getter
@RequiredArgsConstructor
public class AccaoExoneracaoCommand implements Command {
    private final String funcionarioId;
    private final String exoneracaoId;
    private final String accao;
    private final ExoneracaoRequestDTO request;
    private final boolean comoMe;
}
