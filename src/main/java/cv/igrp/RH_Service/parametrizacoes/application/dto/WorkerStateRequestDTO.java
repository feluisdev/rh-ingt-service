package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class WorkerStateRequestDTO {
    private String code;
    private String description;
    private Boolean isCore;
    /** Estado que termina a relação de emprego público (cessação). */
    private Boolean endsEmployment;
    /**
     * Situação administrativa perante o quadro (Lei n.º 20/X/2023, art. 117.º):
     * ACTIVIDADE_NO_QUADRO, ACTIVIDADE_FORA_QUADRO, INACTIVIDADE_NO_QUADRO,
     * INACTIVIDADE_FORA_QUADRO, DISPONIBILIDADE ou APOSENTACAO.
     */
    private String situacaoFuncional;
}
