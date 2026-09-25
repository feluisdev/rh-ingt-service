package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os documentos emitidos de um colaborador; nulo: os do utilizador (/me). */
@Getter
@RequiredArgsConstructor
public class GetDocumentosEmitidosQuery implements Query {
    private final String funcionarioId;
}
