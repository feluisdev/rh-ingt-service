package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Marcação das férias do ano (DL n.º 3/2010, arts. 5.º e 6.º). Substitui a que houver. */
@Data
public class FeriasMarcacaoRequestDTO {
    /** ACORDO (art. 5.º n.º 3) ou FIXADA pelo dirigente (n.º 5). */
    private String origem;
    /** Os períodos; os dias úteis são contados pelo servidor. */
    private List<PeriodoFeriasDTO> periodos = new ArrayList<>();
    /**
     * Obrigatória quando o dirigente impõe o gozo interpolado (art. 5.º n.º 2) e quando se altera o
     * mapa por conveniência de serviço (art. 6.º n.º 2).
     */
    private String fundamentacao;
    /**
     * Só depois de o mapa ter sido dado a conhecer, e aí obrigatório para alterar uma marcação:
     * ACORDO ou CONVENIENCIA_SERVICO (art. 6.º n.º 2).
     */
    private String motivoAlteracao;
}
