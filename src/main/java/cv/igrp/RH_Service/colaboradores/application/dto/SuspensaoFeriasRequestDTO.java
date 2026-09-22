package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Pedido de suspensão de férias (DL n.º 3/2010, art. 8.º).
 */
@Data
public class SuspensaoFeriasRequestDTO {

    /**
     * Data <b>a partir da qual</b> as férias deixam de correr. No caso da doença, é a da entrada
     * do documento comprovativo no serviço (art. 8.º n.º 3). O último dia de férias é a véspera.
     */
    private LocalDate data;

    /**
     * Qual das causas do art. 8.º: parentalidade (n.º 1), doença ou assistência a familiares
     * (n.º 2), ou razões imperiosas de serviço (n.º 5). Texto da instituição, não validado.
     */
    private String motivo;
}
