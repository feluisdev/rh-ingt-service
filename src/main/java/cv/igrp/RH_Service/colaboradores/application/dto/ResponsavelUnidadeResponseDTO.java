package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Responsável de uma unidade orgânica, pelo Lugar que a dirige.
 *
 * <p>Mesmo tratamento do chefe: o {@code estado} distingue PROVIDO, CHEFIA_VAGA e
 * SEM_LUGAR_DE_CHEFIA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ResponsavelUnidadeResponseDTO {

    private String unidadeId;

    private String unidadeNome;

    private String positionId;

    private String numeroLugar;

    private String responsavelFuncionarioId;

    private String responsavelNome;

    /** PROVIDO · CHEFIA_VAGA · SEM_LUGAR_DE_CHEFIA. */
    private String estado;
}
