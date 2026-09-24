package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DecisaoPedidoAusenciaRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** A chefia directa aprova ou rejeita um pedido de ausência da sua equipa ({@code /me/equipa}). */
@Getter
@RequiredArgsConstructor
public class DecidirPedidoAusenciaEquipaCommand implements Command {
    private final String pedidoId;
    private final boolean aprovar;
    private final DecisaoPedidoAusenciaRequestDTO request;
}
