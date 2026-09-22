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
public class LeaveTypeRequestDTO {

    @NotBlank
    private String code;

    private String description;

    private boolean deductsBalance;

    private boolean requiresApproval;

    private Integer maxDaysPerYear;

    private String category;

    /**
     * Regime legal do DL n.o 3/2010: {@code FERIAS} (cap. II, vence-se anualmente) ou
     * {@code FALTA} (cap. III). Omitido, mantem o que esta; num tipo novo vale FALTA.
     */
    private String regime;
}
