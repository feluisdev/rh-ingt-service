package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** A mesma relação mensal, em CSV (o «duplicado» do art. 75.º n.º 1, para a folha de cálculo). */
@Getter
@RequiredArgsConstructor
public class GetRelacaoMensalCsvQuery implements Query {
    private final String mes;
    private final String unidadeId;
    private final Boolean incluirSubunidades;
}
