package cv.igrp.RH_Service.colaboradores.domain.filter;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PedidoAusenciaFilter {
    private UUID funcionarioId;
    private String estado;
    private UUID tipoAusenciaId;
    private Integer ano;
}
