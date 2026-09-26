package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Um plano de formação. */
@Getter
@RequiredArgsConstructor
public class GetPlanoFormacaoQuery implements Query {
    private final String planoId;
}
