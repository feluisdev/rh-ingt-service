package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AtualizarFuncionarioRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateFuncionarioCommand implements Command {
    private final AtualizarFuncionarioRequestDTO request;
    private final String funcionarioId;
}
