package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AfectacaoRequestDTO {
    private String funcionarioId;
    private String positionId;
    private String gradeId;
    private String functionId;
    private String origem;         // ADMISSAO|PROGRESSAO|PROMOCAO|MOBILIDADE|TRANSFERENCIA
    private String assignmentType; // PRINCIPAL|ACUMULACAO|SUBSTITUICAO
    private LocalDate dataInicio;
    private String notes;
}
