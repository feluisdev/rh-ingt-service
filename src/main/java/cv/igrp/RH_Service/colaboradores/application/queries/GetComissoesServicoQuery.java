package cv.igrp.RH_Service.colaboradores.application.queries;

import java.time.LocalDate;
import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** As comissões de serviço em curso. */
@Getter
@RequiredArgsConstructor
public class GetComissoesServicoQuery implements Query {
    private final LocalDate terminaAte;
}
