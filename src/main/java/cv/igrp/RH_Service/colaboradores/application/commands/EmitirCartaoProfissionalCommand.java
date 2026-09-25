package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Emitir um cartão profissional novo (substitui o que estiver em uso). */
@Getter
@RequiredArgsConstructor
public class EmitirCartaoProfissionalCommand implements Command {
    private final String funcionarioId;
}
