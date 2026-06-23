package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetGradesQuery implements Query {
    private final String categoryId;
    private final Boolean active;
    private final String pagina;
    private final String tamanho;
    private final String code;
    private final String nome;
}
