package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AtualizarColocacaoRequest;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AtualizarColocacaoCommand implements Command {
    private final String funcionarioId;
    private final String colocacaoId;
    private final AtualizarColocacaoRequest request;
}
