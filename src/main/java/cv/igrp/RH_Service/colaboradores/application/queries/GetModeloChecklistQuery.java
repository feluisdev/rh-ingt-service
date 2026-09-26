package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** O modelo das checklists. */
@Getter
@RequiredArgsConstructor
public class GetModeloChecklistQuery implements Query {
    private final String tipo;
}
