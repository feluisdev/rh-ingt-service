package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class LeaveMobilitySubtypeRequestDTO {

    @NotBlank
    private String code;

    private String description;

    @NotBlank
    private String recordType;

    private boolean affectsPay;

    private boolean countsForSeniority;

    private boolean canSelfSubmit;
}
