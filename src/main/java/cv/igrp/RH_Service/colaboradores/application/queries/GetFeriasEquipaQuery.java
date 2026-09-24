package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As férias do ano da equipa directa da chefia ({@code /me/equipa}). */
@Getter
@RequiredArgsConstructor
public class GetFeriasEquipaQuery implements Query {
    private final int ano;
}
