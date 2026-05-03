package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.PublicHolidayRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdatePublicHolidayCommand implements Command {
    private final PublicHolidayRequestDTO publicHolidayRequest;
    private final String publicHolidayId;
}
