package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os exames de medicina do trabalho do colaborador. */
@Getter
@RequiredArgsConstructor
public class GetExamesSaudeQuery implements Query {
    private final String funcionarioId;
}
