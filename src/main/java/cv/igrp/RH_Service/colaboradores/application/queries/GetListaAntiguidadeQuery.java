package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** A lista de antiguidade de um serviço (art. 69.º do DL n.º 3/2010), em JSON. */
@Getter
@RequiredArgsConstructor
public class GetListaAntiguidadeQuery implements Query {
    private final Integer ano;
    private final String unidadeId;
    private final Boolean incluirSubunidades;
}
