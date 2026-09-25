package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os provimentos e os períodos de prova de um colaborador. */
@Getter
@RequiredArgsConstructor
public class GetEntradaServicoQuery implements Query {
    private final String funcionarioId;
}
