package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class LeaveTypeRequestDTO {

    @NotBlank
    private String code;

    private String description;

    private boolean deductsBalance;

    private boolean requiresApproval;

    private Integer maxDaysPerYear;

    private UUID categoryOptionId;
}
