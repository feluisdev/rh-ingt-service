package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/** O mapa de efectivos de um serviço (Lei n.º 20/X/2023, art. 4.º al. aa)), no dia de hoje. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class MapaEfectivosDTO {
    private LocalDate data;
    private String unidadeId;
    private String unidadeNome;
    private boolean incluirSubunidades;
    /** Lugares activos (a dotação). */
    private int lugares;
    /** Com titular. */
    private int providos;
    private int vagos;
    private int congelados;
    private List<UnidadeEfectivosDTO> unidades;
}
