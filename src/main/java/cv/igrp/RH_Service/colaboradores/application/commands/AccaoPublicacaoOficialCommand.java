package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PublicacaoOficialRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Gerar o extracto (EXTRACTO), registar a publicação (PUBLICADA) ou cancelar (CANCELAR). */
@Getter
@RequiredArgsConstructor
public class AccaoPublicacaoOficialCommand implements Command {
    private final String publicacaoId;
    private final String accao;
    private final PublicacaoOficialRequestDTO request;
}
