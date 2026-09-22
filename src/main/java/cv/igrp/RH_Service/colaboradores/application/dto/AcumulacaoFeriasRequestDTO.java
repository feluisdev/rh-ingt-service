package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

/**
 * Pedido de acumulação de férias para o ano seguinte (DL n.º 3/2010, art. 7.º n.º 1).
 */
@Data
public class AcumulacaoFeriasRequestDTO {

    /** Quantos dias do ano deste saldo passam para o ano seguinte. */
    private Integer dias;

    /**
     * Porque é que não puderam ser gozados. A lei exige que haja um motivo de serviço; o conteúdo
     * é da instituição e não é validado.
     */
    private String motivo;
}
