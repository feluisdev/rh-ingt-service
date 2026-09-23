package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Terminar antes do fim um pedido em horas aprovado: deixa de valer a partir de {@code data}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class TerminarPedidoRequestDTO {
    private LocalDate data;
    private String motivo;
}
