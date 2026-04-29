package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListWorkerStatesQuery implements Query {
    private final String code;
    private final Boolean isActive;
    private final String pagina;
    private final String tamanho;
}
