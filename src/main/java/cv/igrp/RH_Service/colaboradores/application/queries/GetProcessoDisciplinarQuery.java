package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetProcessoDisciplinarQuery implements Query {
    private final String funcionarioId;
    private final String processoId;
}
