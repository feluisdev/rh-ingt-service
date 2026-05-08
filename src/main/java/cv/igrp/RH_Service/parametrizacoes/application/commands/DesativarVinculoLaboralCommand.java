package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DesativarVinculoLaboralCommand implements Command {
    private final String vinculoLaboralId;
}
