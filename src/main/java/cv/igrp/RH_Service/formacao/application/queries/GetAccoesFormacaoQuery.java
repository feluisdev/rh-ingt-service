package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As acções de formação (em /me: as abertas e as minhas). */
@Getter
@RequiredArgsConstructor
public class GetAccoesFormacaoQuery implements Query {
    private final String estado;
    private final Integer ano;
    private final boolean comoMe;
}
