package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetFuncionariosComboboxQuery implements Query {
    private final String q;
    private final String unidadeOrganicaId;
    private final String workerStateId;
}
