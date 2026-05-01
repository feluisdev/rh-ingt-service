package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RegistarColocacaoRequest {
    private String unitId;
    private String jobId;
    @NotNull
    private LocalDate startDate;
    @NotBlank
    private String assignmentType;
    private String notes;
}
