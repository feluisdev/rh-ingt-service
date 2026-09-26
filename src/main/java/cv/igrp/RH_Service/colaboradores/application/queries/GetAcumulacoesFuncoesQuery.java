package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As acumulações de funções (todas, de um colaborador, ou as minhas). */
@Getter
@RequiredArgsConstructor
public class GetAcumulacoesFuncoesQuery implements Query {
    private final String estado;
    private final String funcionarioId;
    private final boolean comoMe;
}
