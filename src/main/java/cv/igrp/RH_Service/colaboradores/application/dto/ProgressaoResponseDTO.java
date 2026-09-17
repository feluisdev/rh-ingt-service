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
public class ProgressaoResponseDTO {
    /** Id da nova afectação criada pela progressão. */
    private String id;
    private String funcionarioId;
    private String positionId;
    private String escalaoAnteriorId;
    private String escalaoAnterior;
    private String escalaoNovoId;
    private String escalaoNovo;
    private LocalDate dataEfeito;
}
