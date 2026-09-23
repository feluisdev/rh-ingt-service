package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PeriodoPresencaDTO {
    /** HH:mm */
    private String entrada;
    /** HH:mm */
    private String saida;
    private int minutos;
}
