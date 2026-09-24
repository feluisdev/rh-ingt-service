package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os indicadores do pessoal de um serviço num ano. */
@Getter
@RequiredArgsConstructor
public class GetIndicadoresPessoalQuery implements Query {
    private final String unidadeId;
    private final Boolean incluirSubunidades;
    private final Integer ano;
}
