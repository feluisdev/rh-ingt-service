package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TipoAusenciaResponseDTO {
    private String id;
    private String nome;
    private String codigo;
    private Boolean deductsBalance;
    private Boolean requiresApproval;
    private Integer maxDaysPerYear;
    /** Limite por acontecimento (V53): art. 15.o n.o 1 do DL n.o 3/2010. Nulo e sem limite. */
    private Integer maxDaysPerOccurrence;
    /** Limite por mes civil (V53): art. 15.o n.o 1 al. o) e al. q). Nulo e sem limite. */
    private Integer maxDaysPerMonth;
    /** Regime legal: FERIAS · FALTA · FALTA_INJUSTIFICADA (V49, V54). */
    private String regime;
    /**
     * O que a ausencia faz a remuneracao (art. 16.o; V54). A aplicacao nao calcula remuneracao:
     * a classificacao existe para o sistema que a processa.
     */
    private String efeitoRemuneracao;
    /** Como se contam os dias (art. 76.º; V56): DIAS_UTEIS · DIAS_SEGUIDOS. */
    private String contagem;
    private Integer maxMinutosPorDia;
    private String categoryOptionCkey;
    private Boolean isActive;
    private String estadoDesc;
}
