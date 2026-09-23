package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resultado da submissão de um pedido de ausência.
 *
 * <p>Leva os <b>dias contados</b> e o <b>estado</b> porque ambos são decididos do lado do
 * servidor: os dias saem do calendário de feriados e fins-de-semana, e o estado inicial
 * depende de o tipo de ausência exigir aprovação.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PedidoAusenciaCriadoResponseDTO {

    /** Id do pedido criado. */
    private String id;

    /** Dias contados pelo servidor. */
    private Integer numeroDias;

    /** Estado inicial do pedido. */
    private String estado;

    /** V58: minutos por dia de um pedido em horas; zero num de dias inteiros. */
    private int minutosPorDia;
}
