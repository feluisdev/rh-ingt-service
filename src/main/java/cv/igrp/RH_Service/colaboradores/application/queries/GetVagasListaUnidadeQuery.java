package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetVagasListaUnidadeQuery implements Query {
    private final String unidadeId;
}
