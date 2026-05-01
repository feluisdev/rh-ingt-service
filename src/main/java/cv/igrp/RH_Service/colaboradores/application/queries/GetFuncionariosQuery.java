package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetFuncionariosQuery implements Query {
    private final String nome;
    private final String nif;
    private final String situacaoProfissional;
    private final String unidadeOrganicaId;
    private final String careerId;
    private final Boolean active;
    private final String pagina;
    private final String tamanho;
}
