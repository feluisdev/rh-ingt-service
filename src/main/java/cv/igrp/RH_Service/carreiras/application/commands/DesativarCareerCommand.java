package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DesativarCareerCommand implements Command {
    private final String careerId;
}
