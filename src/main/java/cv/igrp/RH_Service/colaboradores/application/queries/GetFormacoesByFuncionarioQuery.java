package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetFormacoesByFuncionarioQuery implements Query {
    private final String funcionarioId;
    private final Integer year;
}
