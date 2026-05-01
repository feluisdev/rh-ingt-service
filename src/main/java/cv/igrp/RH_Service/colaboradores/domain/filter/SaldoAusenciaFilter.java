package cv.igrp.RH_Service.colaboradores.domain.filter;

import lombok.Data;

import java.util.UUID;

@Data
public class SaldoAusenciaFilter {
    private UUID funcionarioId;
    private Integer ano;
    private UUID tipoAusenciaId;
}
