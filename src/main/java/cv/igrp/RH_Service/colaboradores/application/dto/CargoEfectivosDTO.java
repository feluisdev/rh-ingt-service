package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Um cargo de uma unidade no mapa de efectivos. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class CargoEfectivosDTO {
    private String carreira;
    private String categoria;
    private boolean foraDeGrelha;
    /** Lugares activos (a dotação). */
    private int lugares;
    /** Com titular. */
    private int providos;
    private int vagos;
    private int congelados;
}
