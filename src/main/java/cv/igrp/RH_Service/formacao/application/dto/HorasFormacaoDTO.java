package cv.igrp.RH_Service.formacao.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** As horas de formação de um colaborador no ano. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class HorasFormacaoDTO {
    private String funcionarioId;
    private String funcionarioNome;
    private int horas;
}
