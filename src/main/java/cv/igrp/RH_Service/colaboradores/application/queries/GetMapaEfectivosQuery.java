package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** O mapa de efectivos de um serviço, em JSON. */
@Getter
@RequiredArgsConstructor
public class GetMapaEfectivosQuery implements Query {
    private final String unidadeId;
    private final Boolean incluirSubunidades;
}
