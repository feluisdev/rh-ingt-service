package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** A relação mensal de um serviço (art. 75.º do DL n.º 3/2010), em JSON. */
@Getter
@RequiredArgsConstructor
public class GetRelacaoMensalQuery implements Query {
    private final String mes;
    private final String unidadeId;
    private final Boolean incluirSubunidades;
}
