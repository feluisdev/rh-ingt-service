package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveMobilitySubtypeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeaveMobilitySubtypeCommand implements Command {

    private LeaveMobilitySubtypeRequestDTO leaveMobilitySubtypeRequest;
    private String leaveMobilitySubtypeId;
}
