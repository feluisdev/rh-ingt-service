package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class GetPedidosByFuncionarioQuery implements Query {
    private final String funcionarioId;
    private final String estado;
    private final UUID tipoAusenciaId;
    private final Integer ano;
}
