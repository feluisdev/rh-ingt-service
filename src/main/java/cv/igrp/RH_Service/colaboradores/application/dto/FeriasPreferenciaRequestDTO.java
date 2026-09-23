package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Preferência de férias (DL n.º 3/2010, art. 5.º n.º 4). Substitui a que houver. */
@Data
public class FeriasPreferenciaRequestDTO {
    /** Um ou mais períodos, todos do ano indicado no caminho. */
    private List<PeriodoFeriasDTO> periodos = new ArrayList<>();
    private String observacoes;
}
