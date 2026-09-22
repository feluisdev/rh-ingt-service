package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * O que a acumulação produziu, nos dois anos — quem autoriza precisa de ver os dois lados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcumulacaoFeriasResponseDTO {

    /** Saldo do ano de destino, onde os dias entraram. */
    private String saldoDestinoId;

    private int anoOrigem;
    private int anoDestino;

    /** Dias acumulados nesta operação. */
    private int diasAcumulados;

    /** O que ainda resta por gozar no ano de origem depois da cedência. */
    private int disponivelNaOrigem;

    /** O que passa a estar disponível no ano de destino. */
    private int disponivelNoDestino;
}
