package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProrrogacaoPermanenciaRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Autorizar ou indeferir a permanência para além dos 65 anos. */
@Getter
@RequiredArgsConstructor
public class DecidirProrrogacaoPermanenciaCommand implements Command {
    private final String funcionarioId;
    private final String prorrogacaoId;
    private final boolean autorizar;
    private final ProrrogacaoPermanenciaRequestDTO request;
}
