package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Entregar, devolver ou anular um cartão. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class CartaoProfissionalRequestDTO {
    /** Entregar e devolver (por omissão, hoje). */
    private LocalDate data;
    /** Anular. */
    private String motivo;
}
