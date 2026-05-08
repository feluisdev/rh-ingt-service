package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ContratoRequestDTO {
    private String funcionarioId;
    @NotNull
    private String contractTypeId;
    private String contractNumber;
    @NotNull
    private LocalDate startDate;
    private LocalDate endDate;
    private String legalBase;
    private String notes;
}
