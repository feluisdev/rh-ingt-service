package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListPublicHolidaysQuery implements Query {
    private final Integer year;
    private final Boolean isNational;
    private final String dateFrom;
    private final String dateTo;
    private final Boolean isActive;
    private final String pagina;
    private final String tamanho;
    private final String nome;
}
