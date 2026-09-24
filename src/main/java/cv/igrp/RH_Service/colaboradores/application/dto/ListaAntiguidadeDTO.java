package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/** A lista de antiguidade de um serviço (DL n.º 3/2010, arts. 69.º e 70.º). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ListaAntiguidadeDTO {
    private int ano;
    /** 31 de Dezembro do ano anterior. */
    private LocalDate referencia;
    private String unidadeId;
    private String unidadeNome;
    private boolean incluirSubunidades;
    private List<GrupoAntiguidadeDTO> grupos;
}
