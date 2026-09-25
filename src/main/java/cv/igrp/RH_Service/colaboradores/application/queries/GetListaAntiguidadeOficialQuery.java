package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Uma lista oficial com as linhas e as reclamações. */
@Getter
@RequiredArgsConstructor
public class GetListaAntiguidadeOficialQuery implements Query {
    private final String listaId;
}
