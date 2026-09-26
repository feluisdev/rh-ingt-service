package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Um auto por falta de assiduidade ou abandono de lugar a levantar (arts. 80.º e 81.º). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AutoSugeridoDTO {
    private String funcionarioId;
    private String funcionarioNome;
    /** FALTA_ASSIDUIDADE, ABANDONO_LUGAR (para a participação). */
    private String especie;
    private String motivo;
    /** Maior sequência de dias úteis de falta injustificada no ano. */
    private int seguidos;
    /** Dias úteis de falta injustificada no ano civil. */
    private int noAno;
    /** Dias úteis de falta injustificada nos últimos 24 meses. */
    private int em24Meses;
}
