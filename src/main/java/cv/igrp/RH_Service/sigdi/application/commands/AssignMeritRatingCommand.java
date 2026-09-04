package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.AssignMeritRatingRequestDTO;
import cv.igrp.framework.core.domain.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignMeritRatingCommand implements Command {

    @NotBlank
    private String evaluationId;

    @NotNull
    @Valid
    private AssignMeritRatingRequestDTO body;
}
