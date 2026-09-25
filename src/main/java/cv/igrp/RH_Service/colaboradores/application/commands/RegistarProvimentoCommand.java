package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProvimentoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Registar um provimento (e o período de prova que o acompanha). */
@Getter
@RequiredArgsConstructor
public class RegistarProvimentoCommand implements Command {
    private final String funcionarioId;
    private final ProvimentoRequestDTO request;
}
