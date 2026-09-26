package cv.igrp.RH_Service.recrutamento.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Um concurso com as candidaturas. */
@Getter
@RequiredArgsConstructor
public class GetConcursoQuery implements Query {
    private final String concursoId;
}
