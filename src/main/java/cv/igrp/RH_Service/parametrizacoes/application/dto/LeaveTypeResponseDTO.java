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

    private String category;

    /** Regime legal do DL n.o 3/2010: FERIAS ou FALTA. */
    private String regime;
    private String categoryDesc;

    private Boolean isActive;
    private String estadoDesc;
}
