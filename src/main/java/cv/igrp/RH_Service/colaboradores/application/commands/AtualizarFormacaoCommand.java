package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AtualizarFormacaoRequest;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AtualizarFormacaoCommand implements Command {
    private final String funcionarioId;
    private final String formacaoId;
    private final AtualizarFormacaoRequest request;
}
