package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class LeaveMobilitySubtypeResponseDTO {

    private String id;

    private String code;

    private String description;

    private String recordType;

    private String recordTypeDesc;

    private boolean affectsPay;

    private boolean countsForSeniority;

    private boolean canSelfSubmit;

    private Integer maxDurationDays;

    private Integer maxExtensions;

    /** MANTEM | ABRE_VAGA. */
    private String positionEffect;

    private Integer vacancyAfterDays;

    /** REGRESSA_LUGAR | DISPONIBILIDADE. */
    private String returnEffect;

    private Boolean isActive;
    private String estadoDesc;
}
