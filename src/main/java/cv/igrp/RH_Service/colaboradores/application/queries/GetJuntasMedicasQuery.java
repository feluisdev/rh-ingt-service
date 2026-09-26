package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Os pedidos de junta médica. */
@Getter
@RequiredArgsConstructor
public class GetJuntasMedicasQuery implements Query {
    private final String estado;
    private final String funcionarioId;
}
