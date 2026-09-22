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
    /** Só {@code PRINCIPAL}; a substituição tem endpoint próprio. Omisso vale PRINCIPAL. */
    private String assignmentType;
    private LocalDate dataInicio;
    private String notes;
}
