package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EnquadramentoResponseDTO {
    private String id;
    private String funcionarioId;
    private String careerId;
    private String categoryId;
    private String gradeId;
    private String cargoId;
    private String functionId;
    private String unidadeOrganicaId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Boolean isCurrent;
}
