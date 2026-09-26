package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os planos de formação. */
@Getter
@RequiredArgsConstructor
public class GetPlanosFormacaoQuery implements Query {
    private final Integer ano;
}
