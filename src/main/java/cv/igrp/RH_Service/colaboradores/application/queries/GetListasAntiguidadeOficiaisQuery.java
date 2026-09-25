package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As listas de antiguidade oficiais, filtradas por ano e serviço. */
@Getter
@RequiredArgsConstructor
public class GetListasAntiguidadeOficiaisQuery implements Query {
    private final Integer ano;
    private final String unidadeId;
}
