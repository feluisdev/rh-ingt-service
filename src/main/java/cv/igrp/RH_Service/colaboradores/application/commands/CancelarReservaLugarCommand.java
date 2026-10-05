package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.CancelarReservaLugarRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Cancelar o Lugar reservado de um colaborador. */
@Getter
@RequiredArgsConstructor
public class CancelarReservaLugarCommand implements Command {
    private final String funcionarioId;
    private final CancelarReservaLugarRequestDTO request;
}
