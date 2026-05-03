package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetCareersQuery implements Query {
    private final Boolean active;
    private final String pagina;
    private final String tamanho;
}
