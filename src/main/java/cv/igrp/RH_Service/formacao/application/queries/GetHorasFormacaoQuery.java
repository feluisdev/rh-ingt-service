package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As horas de formação por colaborador no ano. */
@Getter
@RequiredArgsConstructor
public class GetHorasFormacaoQuery implements Query {
    private final Integer ano;
}
