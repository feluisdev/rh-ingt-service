package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Reabrir um mês. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FechoMensalRequestDTO {
    private String motivo;
}
