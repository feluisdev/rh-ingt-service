package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetRecibosByFuncionarioQuery implements Query {
    private final String funcionarioId;
    private final Integer periodYear;
    private final Integer periodMonth;
}
