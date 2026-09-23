package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** O mapa de férias do ano (DL n.º 3/2010, art. 6.º). */
@Data
public class MapaFeriasResponseDTO {
    private int ano;
    /** Art. 6.º n.º 1: até quando o serviço deve elaborar o mapa e dar conhecimento. */
    private LocalDate prazoElaboracao;
    /** Quando se deu conhecimento; nulo enquanto está a ser elaborado. */
    private LocalDate publicadoEm;
    private List<MapaFeriasLinhaDTO> linhas = new ArrayList<>();
    /**
     * Colaboradores activos sem férias marcadas. Sem acordo, é a estes que o dirigente fixa as
     * férias entre 1 de Maio e 31 de Outubro (art. 5.º n.º 5).
     */
    private List<MapaFeriasLinhaDTO> semMarcacao = new ArrayList<>();
}
