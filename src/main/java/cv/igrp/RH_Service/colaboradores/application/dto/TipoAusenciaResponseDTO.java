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
    private String categoryOptionCkey;
    private Boolean isActive;
    private String estadoDesc;
}
