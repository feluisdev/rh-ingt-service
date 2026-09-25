package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os pedidos de declaração de um colaborador (funcionarioId nulo: o do utilizador), ou todos os por emitir (porEmitir). */
@Getter
@RequiredArgsConstructor
public class GetDeclaracoesQuery implements Query {
    private final String funcionarioId;
    private final boolean porEmitir;
}
