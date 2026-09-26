package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AcidenteServicoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Um passo do acidente em serviço (comoMe: o próprio participa em /me). */
@Getter
@RequiredArgsConstructor
public class AccaoAcidenteServicoCommand implements Command {
    private final String funcionarioId;
    private final String acidenteId;
    private final String accao;
    private final AcidenteServicoRequestDTO request;
    private final boolean comoMe;
}
