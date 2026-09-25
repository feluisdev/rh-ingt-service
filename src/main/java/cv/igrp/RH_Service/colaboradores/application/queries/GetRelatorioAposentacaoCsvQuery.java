package cv.igrp.RH_Service.colaboradores.application.queries;

import java.time.LocalDate;
import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** O mesmo relatório em CSV. */
@Getter
@RequiredArgsConstructor
public class GetRelatorioAposentacaoCsvQuery implements Query {
    private final String unidadeId;
    private final Boolean incluirSubunidades;
    private final LocalDate ate;
}
