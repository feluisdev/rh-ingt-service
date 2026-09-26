package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** A exportação de um mês para o salarial. */
@Getter
@RequiredArgsConstructor
public class GetExportacaoSalarialQuery implements Query {
    private final String mes;
}
