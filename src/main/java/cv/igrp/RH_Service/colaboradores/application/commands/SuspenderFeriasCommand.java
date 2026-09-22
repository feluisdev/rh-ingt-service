package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class SuspenderFeriasCommand implements Command {
    private final String funcionarioId;
    private final String pedidoId;
    private final LocalDate data;
    private final String motivo;
}
