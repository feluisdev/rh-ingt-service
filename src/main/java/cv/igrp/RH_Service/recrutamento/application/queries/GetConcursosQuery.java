package cv.igrp.RH_Service.recrutamento.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os concursos, por estado. */
@Getter
@RequiredArgsConstructor
public class GetConcursosQuery implements Query {
    private final String estado;
}
