package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Um passo do ciclo da lista: AFIXAR, RECALCULAR, DEFINITIVA, PUBLICAR ou ANULAR. */
@Getter
@RequiredArgsConstructor
public class AccaoListaAntiguidadeCommand implements Command {
    private final String listaId;
    private final String accao;
    private final ListaAntiguidadeRequestDTO request;
}
