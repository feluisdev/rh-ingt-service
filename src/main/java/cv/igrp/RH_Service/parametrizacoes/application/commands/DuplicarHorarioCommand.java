package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Uma cópia editável de um horário (o que já vigorou não se edita: duplica-se). */
@Getter
@RequiredArgsConstructor
public class DuplicarHorarioCommand implements Command {
    private final String horarioId;
    /** Opcional; por omissão, o nome do original com «(cópia)». */
    private final String nome;
}
