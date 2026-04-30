package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AtivarCategoryCommand implements Command {
    private final String categoryId;
}
