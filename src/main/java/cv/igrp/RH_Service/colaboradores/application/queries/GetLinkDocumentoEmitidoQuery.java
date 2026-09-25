package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** O link assinado para descarregar o PDF; doProprio: só se for do utilizador. */
@Getter
@RequiredArgsConstructor
public class GetLinkDocumentoEmitidoQuery implements Query {
    private final String documentoId;
    private final boolean doProprio;
}
