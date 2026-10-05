package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Cancelar a reserva de um Lugar. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class CancelarReservaLugarRequestDTO {
    private String motivo;
}
