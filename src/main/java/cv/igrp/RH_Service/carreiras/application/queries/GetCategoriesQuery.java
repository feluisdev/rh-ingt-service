package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetCategoriesQuery implements Query {
    private final String careerId;
    private final Boolean active;
    private final String pagina;
    private final String tamanho;
}
