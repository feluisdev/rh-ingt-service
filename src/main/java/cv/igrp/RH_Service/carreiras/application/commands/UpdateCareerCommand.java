package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.application.dto.CareerRequest;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateCareerCommand implements Command {
    private final CareerRequest request;
    private final String careerId;
}
