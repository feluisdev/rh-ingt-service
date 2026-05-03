package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateOrganizationalUnitCommand implements Command {
    private final String unitId;
    private final OrganizationalUnitRequestDTO request;
}
