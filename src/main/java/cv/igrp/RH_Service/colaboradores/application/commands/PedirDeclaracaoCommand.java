package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoDeclaracaoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Pedir uma declaração: o próprio (fica por emitir) ou o RH (emite logo). */
@Getter
@RequiredArgsConstructor
public class PedirDeclaracaoCommand implements Command {
    private final boolean peloProprio;
    private final String funcionarioId;
    private final PedidoDeclaracaoRequestDTO request;
}
