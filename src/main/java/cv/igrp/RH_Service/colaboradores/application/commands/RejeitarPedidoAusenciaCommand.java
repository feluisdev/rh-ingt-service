package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.RejeitarPedidoRequest;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RejeitarPedidoAusenciaCommand implements Command {
    private final String funcionarioId;
    private final String pedidoId;
    private final RejeitarPedidoRequest request;
}
