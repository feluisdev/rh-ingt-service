package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class CloseContratoCommand implements Command {
    private final String contratoId;
    private final LocalDate endDate;
    private final String terminationReason;
    private final String notes;
}
