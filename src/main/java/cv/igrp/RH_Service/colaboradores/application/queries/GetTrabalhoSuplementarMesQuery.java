package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** O trabalho suplementar de um mês. {@code funcionarioId} nulo: o do utilizador ({@code /me}). */
@Getter
@RequiredArgsConstructor
public class GetTrabalhoSuplementarMesQuery implements Query {
    private final String funcionarioId;
    private final String mes;
}
