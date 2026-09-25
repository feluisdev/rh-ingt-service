package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Aprovar (e congelar) a lista de antiguidade de um serviço num ano. */
@Getter
@RequiredArgsConstructor
public class AprovarListaAntiguidadeCommand implements Command {
    private final ListaAntiguidadeRequestDTO request;
}
