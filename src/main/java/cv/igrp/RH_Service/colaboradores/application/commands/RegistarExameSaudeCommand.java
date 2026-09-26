package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ExameSaudeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Registar um exame de medicina do trabalho. */
@Getter
@RequiredArgsConstructor
public class RegistarExameSaudeCommand implements Command {
    private final String funcionarioId;
    private final ExameSaudeRequestDTO request;
}
