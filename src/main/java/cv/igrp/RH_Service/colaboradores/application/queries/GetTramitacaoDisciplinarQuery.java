package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** O processo disciplinar com a tramitação. */
@Getter
@RequiredArgsConstructor
public class GetTramitacaoDisciplinarQuery implements Query {
    private final String funcionarioId;
    private final String processoId;
}
