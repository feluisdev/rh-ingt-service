package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPositionsByUnidadeQuery implements Query {
    private final String unidadeId;
}
