package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ProfessionalSituationRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateProfessionalSituationCommand implements Command {
    private final ProfessionalSituationRequestDTO professionalSituationRequest;
    private final String professionalSituationId;
}
