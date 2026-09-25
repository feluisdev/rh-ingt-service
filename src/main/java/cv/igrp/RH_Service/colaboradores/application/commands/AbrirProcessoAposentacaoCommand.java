package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProcessoAposentacaoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Abrir um processo de aposentação: pelo RH ou pelo próprio (/me), que só pede a antecipada ou a pré-aposentação. */
@Getter
@RequiredArgsConstructor
public class AbrirProcessoAposentacaoCommand implements Command {
    private final boolean peloProprio;
    private final String funcionarioId;
    private final ProcessoAposentacaoRequestDTO request;
}
