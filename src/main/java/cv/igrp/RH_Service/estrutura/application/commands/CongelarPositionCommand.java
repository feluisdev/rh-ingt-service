package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CongelarPositionCommand implements Command {
    private final String positionId;
}
