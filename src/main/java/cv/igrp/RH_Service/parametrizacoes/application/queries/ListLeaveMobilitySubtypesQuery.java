package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListLeaveMobilitySubtypesQuery implements Query {

    private String code;
    private String recordType;
    private Boolean active;
    private String pagina;
    private String tamanho;
}
