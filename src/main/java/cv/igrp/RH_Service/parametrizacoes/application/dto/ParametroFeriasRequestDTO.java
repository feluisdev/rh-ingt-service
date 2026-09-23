package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ParametroFeriasRequestDTO {
    private Integer vigenteDesde;
    private String prazoPreferencia;
    private String prazoMapa;
    private String fixacaoInicio;
    private String fixacaoFim;
    private Integer periodoMinimoInterpolado;
    private String fundamento;
}
