package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ContratoResponseDTO {
    private String id;
    private String funcionarioId;
    private String contractTypeId;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String terminationReason;
    private Boolean isCurrent;
    private String status;
    private Integer renewalCount;
    private String legalBase;
    private String notes;
}
