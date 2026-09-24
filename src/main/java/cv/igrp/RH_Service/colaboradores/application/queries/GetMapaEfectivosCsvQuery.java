package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** O mapa de efectivos de um serviço, em CSV. */
@Getter
@RequiredArgsConstructor
public class GetMapaEfectivosCsvQuery implements Query {
    private final String unidadeId;
    private final Boolean incluirSubunidades;
}
