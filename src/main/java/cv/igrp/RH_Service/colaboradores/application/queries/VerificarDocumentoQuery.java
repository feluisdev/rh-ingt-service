package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Verificação pública de um documento pelo código. */
@Getter
@RequiredArgsConstructor
public class VerificarDocumentoQuery implements Query {
    private final String codigo;
}
