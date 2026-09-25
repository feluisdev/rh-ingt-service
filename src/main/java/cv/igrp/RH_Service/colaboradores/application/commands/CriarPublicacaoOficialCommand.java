package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PublicacaoOficialRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Criar à mão um acto a publicar. */
@Getter
@RequiredArgsConstructor
public class CriarPublicacaoOficialCommand implements Command {
    private final PublicacaoOficialRequestDTO request;
}
