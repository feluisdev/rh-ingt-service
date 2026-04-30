package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.application.dto.GradeRequest;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateGradeCommand implements Command {
    private final GradeRequest request;
}
