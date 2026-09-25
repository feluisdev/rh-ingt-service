package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoDeclaracaoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Emitir ou recusar um pedido de declaração (RH). */
@Getter
@RequiredArgsConstructor
public class DecidirDeclaracaoCommand implements Command {
    private final String funcionarioId;
    private final String pedidoId;
    private final boolean emitir;
    private final PedidoDeclaracaoRequestDTO request;
}
