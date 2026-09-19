package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Onde o colaborador está e onde exerce funções.
 *
 * <p>São duas coisas diferentes e é por isso que há dois pares de campos: o Lugar é
 * sempre o do titular, mas quem está em mobilidade <b>exerce funções noutro sítio</b>
 * sem perder o Lugar (art. 135.º n.º 7).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class UnidadeAtualResponseDTO {

    private String funcionarioId;

    private String funcionarioNome;

    /** Lugar de que é titular. */
    private String positionId;

    private String numeroLugar;

    private String unidadeOrganicaId;

    private String unidadeNome;

    private String jobId;

    private String jobNome;

    /** Está a exercer funções fora do seu Lugar. */
    private Boolean emMobilidade;

    private String mobilidadeId;

    private LocalDate mobilidadeInicio;

    private LocalDate mobilidadeFim;

    /** INTERNO ou EXTERNO. */
    private String mobilidadeDestinoTipo;

    /** Onde exerce hoje; igual à unidade do Lugar se não estiver em mobilidade. */
    private String exerceFuncoesUnidadeId;

    private String exerceFuncoesUnidadeNome;
}
