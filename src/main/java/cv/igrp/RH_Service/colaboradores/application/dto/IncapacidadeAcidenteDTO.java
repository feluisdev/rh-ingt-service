package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um período de incapacidade temporária. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class IncapacidadeAcidenteDTO {
    private String id;
    /** TEMPORARIA_ABSOLUTA, TEMPORARIA_PARCIAL */
    private String tipo;
    private LocalDate inicio;
    /** Vazio enquanto não há alta. */
    private LocalDate fim;
}
