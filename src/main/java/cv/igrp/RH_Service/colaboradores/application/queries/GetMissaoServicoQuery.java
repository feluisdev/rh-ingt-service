package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Uma missão de serviço. */
@Getter
@RequiredArgsConstructor
public class GetMissaoServicoQuery implements Query {
    private final String missaoId;
}
