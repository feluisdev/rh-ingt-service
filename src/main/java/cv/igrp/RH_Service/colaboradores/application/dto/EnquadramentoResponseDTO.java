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
    private String careerName;
    private String categoryId;
    private String categoryName;
    private String gradeId;
    private String gradeName;
    private String cargoId;
    private String cargoName;
    private String functionId;
    private String functionName;
    private String unidadeOrganicaId;
    private String unitName;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Boolean isCurrent;
    private String isCurrentDesc;
}
