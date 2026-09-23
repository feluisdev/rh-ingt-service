package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class HorarioBlocoDTO {
    /** 1 = segunda ... 7 = domingo (ISO-8601). */
    private Integer diaSemana;
    /** HH:mm */
    private String inicio;
    /** HH:mm */
    private String fim;
    /** Plataforma fixa, no flexível. No fixo é sempre verdadeiro; omisso = verdadeiro. */
    private Boolean obrigatorio;
}
