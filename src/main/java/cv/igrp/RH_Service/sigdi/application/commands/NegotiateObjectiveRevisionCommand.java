package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.NegotiateObjectiveRevisionRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NegotiateObjectiveRevisionCommand implements Command {
    private String evaluationId;
    private String revisionId;
    private NegotiateObjectiveRevisionRequestDTO body;
}
