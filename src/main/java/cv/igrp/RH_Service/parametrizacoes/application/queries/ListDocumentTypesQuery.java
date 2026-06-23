package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListDocumentTypesQuery implements Query {

    private String codigo;
    private Boolean active;
    private String pagina;
    private String tamanho;
    private String nome;
}
