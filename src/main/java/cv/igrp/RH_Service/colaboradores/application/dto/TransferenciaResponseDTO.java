package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaResponseDTO {
    /** Id da nova afectação criada pela transferência. */
    private String id;
    private String funcionarioId;
    private String positionAnteriorId;
    private String numeroLugarAnterior;
    private String unidadeOrganicaAnteriorId;
    private String positionId;
    private String numeroLugar;
    private String unidadeOrganicaId;
    private String functionId;
    private LocalDate dataEfeito;
}
