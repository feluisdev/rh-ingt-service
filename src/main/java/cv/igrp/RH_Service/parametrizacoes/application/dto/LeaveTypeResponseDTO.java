package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class LeaveTypeResponseDTO {

    private String id;

    private String code;

    private String description;

    private boolean deductsBalance;

    private boolean requiresApproval;

    private Integer maxDaysPerYear;

    /**
     * Limite por ACONTECIMENTO (V53): 6 dias por ocasiao do casamento, 8 por falecimento do
     * conjuge, 2 por cada prova (art. 15.o n.o 1 do DL n.o 3/2010). Nao se soma ao ano -- quem
     * perde dois familiares no mesmo ano tem direito as duas ausencias. Nulo e sem limite.
     */
    private Integer maxDaysPerOccurrence;

    /** Limite por MES civil (V53): art. 15.o n.o 1 al. o) e al. q). Nulo e sem limite. */
    private Integer maxDaysPerMonth;

    private String category;

    /** Regime legal do DL n.o 3/2010: FERIAS · FALTA · FALTA_INJUSTIFICADA. */
    private String regime;

    /**
     * O que a ausencia faz a remuneracao (art. 16.o do DL n.o 3/2010): SEM_PERDA ·
     * PERDA_PARCIAL · PERDA_TOTAL · PERDA_VENCIMENTO_EXERCICIO · DEPENDE_DA_OPCAO. Esta
     * aplicacao NAO calcula remuneracao -- a classificacao existe para o sistema que a processa.
     */
    private String efeitoRemuneracao;

    private String categoryDesc;

    private Boolean isActive;
    private String estadoDesc;
}
