package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ReclamacaoAntiguidadeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Decidir a reclamação (DECIDIR), recorrer (RECORRER) ou decidir o recurso (DECIDIR_RECURSO). */
@Getter
@RequiredArgsConstructor
public class AccaoReclamacaoAntiguidadeCommand implements Command {
    private final String listaId;
    private final String reclamacaoId;
    private final String accao;
    private final ReclamacaoAntiguidadeRequestDTO request;
}
