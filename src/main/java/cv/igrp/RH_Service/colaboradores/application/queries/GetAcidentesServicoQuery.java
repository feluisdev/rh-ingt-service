package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os acidentes em serviço (todos, de um colaborador, ou os meus). */
@Getter
@RequiredArgsConstructor
public class GetAcidentesServicoQuery implements Query {
    private final String estado;
    private final String funcionarioId;
    private final boolean comoMe;
}
