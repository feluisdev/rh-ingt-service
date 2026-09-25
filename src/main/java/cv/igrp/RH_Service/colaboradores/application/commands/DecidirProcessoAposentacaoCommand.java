package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DecisaoAposentacaoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Um passo do processo de aposentação: DEFERIR, INDEFERIR, DESLIGAR, CONCLUIR ou CANCELAR. */
@Getter
@RequiredArgsConstructor
public class DecidirProcessoAposentacaoCommand implements Command {
    private final String funcionarioId;
    private final String processoId;
    private final String accao;
    private final DecisaoAposentacaoRequestDTO request;
}
