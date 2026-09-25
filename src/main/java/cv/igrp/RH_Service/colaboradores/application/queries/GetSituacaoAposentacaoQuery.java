package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** A situação de um colaborador perante a aposentação; funcionarioId nulo: o do utilizador (/me). */
@Getter
@RequiredArgsConstructor
public class GetSituacaoAposentacaoQuery implements Query {
    private final String funcionarioId;
}
