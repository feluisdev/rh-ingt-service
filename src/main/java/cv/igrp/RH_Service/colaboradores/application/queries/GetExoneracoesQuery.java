package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As exonerações voluntárias (todas, de um colaborador, ou as minhas). */
@Getter
@RequiredArgsConstructor
public class GetExoneracoesQuery implements Query {
    private final String estado;
    private final String funcionarioId;
    private final boolean comoMe;
}
