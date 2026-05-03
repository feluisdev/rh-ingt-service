package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejeitarLicencaMobilidadeRequestDTO {
    @NotBlank
    private String rejectionReason;
}
