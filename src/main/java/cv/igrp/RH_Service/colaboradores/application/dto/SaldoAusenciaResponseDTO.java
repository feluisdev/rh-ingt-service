package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

@Data
public class SaldoAusenciaResponseDTO {
    private String id;
    private String funcionarioId;
    private String tipoAusenciaId;
    private TipoAusenciaResponseDTO tipoAusencia;
    private int ano;
    private int diasDireito;
    private int diasGozados;
    private int diasPendentes;
    private int diasDisponiveis;
    /** Dias recebidos do ano anterior por acumulacao (art. 7.o n.o 1 do DL n.o 3/2010). */
    private int diasAcumulados;
    /** Porque e que esses dias nao puderam ser gozados no ano em que se venceram. */
    private String acumulacaoMotivo;
    /** Dias ja cedidos ao ano seguinte. */
    private int diasTransportados;
    /** Quantos dias deste ano ainda podem ser acumulados para o seguinte. */
    private int diasAcumulaveis;
}
