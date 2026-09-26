package cv.igrp.RH_Service.recrutamento.application.commands;

import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Criar (concursoId nulo) ou editar um concurso em rascunho. */
@Getter
@RequiredArgsConstructor
public class GuardarConcursoCommand implements Command {
    private final String concursoId;
    private final ConcursoRequestDTO request;
}
