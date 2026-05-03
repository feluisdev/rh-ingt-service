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

    private boolean affectsPay;

    private boolean countsForSeniority;

    private boolean canSelfSubmit;

    private Boolean isActive;
}
