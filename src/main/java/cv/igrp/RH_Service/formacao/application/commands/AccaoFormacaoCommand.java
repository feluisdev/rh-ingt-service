package cv.igrp.RH_Service.formacao.application.commands;

import cv.igrp.RH_Service.formacao.application.dto.AccaoFormacaoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Um passo da acção de formação ou de uma inscrição (comoMe: a chefia ou o próprio em /me). */
@Getter
@RequiredArgsConstructor
public class AccaoFormacaoCommand implements Command {
    private final String accaoId;
    private final String inscricaoId;
    private final String accao;
    private final AccaoFormacaoRequestDTO request;
    private final boolean comoMe;
}
