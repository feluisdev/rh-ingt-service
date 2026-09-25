package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AnulacaoDocumentoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Anular um documento emitido, com motivo. */
@Getter
@RequiredArgsConstructor
public class AnularDocumentoEmitidoCommand implements Command {
    private final String documentoId;
    private final AnulacaoDocumentoRequestDTO request;
}
