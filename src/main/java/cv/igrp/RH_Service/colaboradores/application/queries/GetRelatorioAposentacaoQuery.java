package cv.igrp.RH_Service.colaboradores.application.queries;

import java.time.LocalDate;
import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Quem, no serviço, atinge o limite de idade ou reúne as condições da antecipada ou da pré-aposentação até uma data. */
@Getter
@RequiredArgsConstructor
public class GetRelatorioAposentacaoQuery implements Query {
    private final String unidadeId;
    private final Boolean incluirSubunidades;
    private final LocalDate ate;
}
