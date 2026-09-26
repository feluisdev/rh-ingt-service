package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As missões de serviço (em /me: as minhas e as pedidas da equipa). */
@Getter
@RequiredArgsConstructor
public class GetMissoesServicoQuery implements Query {
    private final String estado;
    private final String funcionarioId;
    private final boolean comoMe;
}
