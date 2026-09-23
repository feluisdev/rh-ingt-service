package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class GetHorarioVigenteQuery implements Query {
    private final String funcionarioId;
    /** Nula: hoje. */
    private final LocalDate data;
}
