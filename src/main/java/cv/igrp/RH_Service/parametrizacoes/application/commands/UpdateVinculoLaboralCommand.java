package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.VinculoLaboralRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateVinculoLaboralCommand implements Command {
    private final VinculoLaboralRequestDTO vinculoLaboralRequest;
    private final String vinculoLaboralId;
}
