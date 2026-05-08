package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ContratoRequestDTO {
    @NotNull
    private String contractTypeId;
    private String contractNumber;
    @NotNull
    private LocalDate startDate;
    private LocalDate endDate;
    private String regimeTrabalho;
    private BigDecimal percentagemTempo;
    private String legalBase;
    private String notes;
}
