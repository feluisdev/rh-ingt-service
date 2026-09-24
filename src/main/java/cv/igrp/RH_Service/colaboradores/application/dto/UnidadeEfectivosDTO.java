package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/** Uma unidade orgânica no mapa de efectivos, com os cargos e os totais. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class UnidadeEfectivosDTO {
    private String unidadeId;
    private String codigo;
    private String nome;
    /** Lugares activos (a dotação). */
    private int lugares;
    /** Com titular. */
    private int providos;
    private int vagos;
    private int congelados;
    private List<CargoEfectivosDTO> cargos;
}
