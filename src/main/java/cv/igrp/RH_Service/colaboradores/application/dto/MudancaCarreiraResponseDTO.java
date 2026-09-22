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
public class MudancaCarreiraResponseDTO {
    /** Id da nova afectação criada pela mudança de carreira. */
    private String id;
    private String funcionarioId;
    private String positionAnteriorId;
    private String numeroLugarAnterior;
    private String carreiraAnteriorId;
    private String carreiraAnterior;
    private String categoriaAnteriorId;
    private String categoriaAnterior;
    private String positionId;
    private String numeroLugar;
    private String unidadeOrganicaId;
    private String carreiraNovaId;
    private String carreiraNova;
    private String categoriaNovaId;
    private String categoriaNova;
    private String escalaoId;
    private String escalao;
    private String functionId;
    private LocalDate dataEfeito;
}
