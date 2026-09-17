package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ContratoResponseDTO {
    private String id;
    private String funcionarioId;
    private String contractTypeId;
    private String contractTypeName;
    // Vínculo laboral — derivado do tipo de contrato, nunca guardado no contrato nem no funcionário.
    private String vinculoLaboralId;
    private String vinculoLaboralCode;
    private String vinculoLaboralDesc;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String terminationReason;
    private Boolean isCurrent;
    private String isCurrentDesc;
    private String status;
    private Integer renewalCount;
    private String regimeTrabalho;
    private BigDecimal percentagemTempo;
    private String legalBase;
    private String notes;
}
