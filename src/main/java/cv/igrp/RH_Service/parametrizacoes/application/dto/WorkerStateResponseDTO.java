package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class WorkerStateResponseDTO {
    private String id;
    private String code;
    private String description;
    private Boolean isCore;
    private Boolean isActive;
    private Boolean endsEmployment;
    /** Situação administrativa perante o quadro (art. 117.º); nula se não classificada. */
    private String situacaoFuncional;
    private String estadoDesc;
}
