package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As checklists do colaborador; sem funcionarioId, as minhas e as da minha equipa. */
@Getter
@RequiredArgsConstructor
public class GetChecklistsQuery implements Query {
    private final String funcionarioId;
}
