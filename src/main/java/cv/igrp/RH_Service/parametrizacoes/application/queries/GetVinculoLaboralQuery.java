package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetVinculoLaboralQuery implements Query {
    private final String vinculoLaboralId;
}
