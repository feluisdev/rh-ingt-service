package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

@Data
public class SaldoAusenciaResponse {
    private String id;
    private String funcionarioId;
    private String tipoAusenciaId;
    private TipoAusenciaResponse tipoAusencia;
    private int ano;
    private int diasDireito;
    private int diasGozados;
    private int diasPendentes;
    private int diasDisponiveis;
}
