package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os factos de um mês de processamento em CSV. */
@Getter
@RequiredArgsConstructor
public class GetFactosSalariaisCsvQuery implements Query {
    private final String mes;
}
