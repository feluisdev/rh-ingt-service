package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.RecordObjectiveAchievementRequestDTO;
import cv.igrp.framework.core.domain.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordObjectiveAchievementCommand implements Command {

    @NotNull
    @Valid
    private RecordObjectiveAchievementRequestDTO body;
}
