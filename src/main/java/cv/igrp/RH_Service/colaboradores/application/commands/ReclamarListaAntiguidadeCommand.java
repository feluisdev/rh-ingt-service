package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ReclamacaoAntiguidadeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Apresentar uma reclamação da lista: pelo RH (funcionarioId no corpo) ou pelo próprio. */
@Getter
@RequiredArgsConstructor
public class ReclamarListaAntiguidadeCommand implements Command {
    private final boolean peloProprio;
    private final String listaId;
    private final ReclamacaoAntiguidadeRequestDTO request;
}
