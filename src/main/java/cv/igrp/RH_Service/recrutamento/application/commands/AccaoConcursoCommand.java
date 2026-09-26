package cv.igrp.RH_Service.recrutamento.application.commands;

import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** ABRIR, ENCERRAR, AVALIAR, LISTA_PROVISORIA, HOMOLOGAR, CONCLUIR ou ANULAR. */
@Getter
@RequiredArgsConstructor
public class AccaoConcursoCommand implements Command {
    private final String concursoId;
    private final String accao;
    private final ConcursoRequestDTO request;
}
