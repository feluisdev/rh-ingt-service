package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Uma acção de formação. */
@Getter
@RequiredArgsConstructor
public class GetAccaoFormacaoQuery implements Query {
    private final String accaoId;
}
