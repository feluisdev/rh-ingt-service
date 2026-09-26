package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** A lista de trabalho: checklists abertas com itens pendentes. */
@Getter
@RequiredArgsConstructor
public class GetChecklistsPendentesQuery implements Query {
    private final String tipo;
    private final String responsavel;
    private final Boolean atrasadas;
}
