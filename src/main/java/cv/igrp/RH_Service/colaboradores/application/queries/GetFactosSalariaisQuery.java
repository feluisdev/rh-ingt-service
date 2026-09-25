package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os factos de um mês de processamento ({@code mes}) ou os de um colaborador ({@code funcionarioId}). */
@Getter
@RequiredArgsConstructor
public class GetFactosSalariaisQuery implements Query {
    private final String mes;
    private final String funcionarioId;
}
