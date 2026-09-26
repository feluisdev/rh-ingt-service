package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.FechoMensalRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** FECHAR ou REABRIR um mês de processamento. */
@Getter
@RequiredArgsConstructor
public class AccaoFechoMensalCommand implements Command {
    private final String mes;
    private final String accao;
    private final FechoMensalRequestDTO request;
}
