package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os actos a publicar, por estado, ou os de um colaborador. */
@Getter
@RequiredArgsConstructor
public class GetPublicacoesOficiaisQuery implements Query {
    private final String estado;
    private final String funcionarioId;
}
