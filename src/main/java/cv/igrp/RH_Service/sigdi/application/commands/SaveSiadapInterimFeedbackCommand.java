package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveSiadapInterimFeedbackCommand implements Command {

    private String evaluationId;
    private SiadapInterimFeedbackDTO body;
}
