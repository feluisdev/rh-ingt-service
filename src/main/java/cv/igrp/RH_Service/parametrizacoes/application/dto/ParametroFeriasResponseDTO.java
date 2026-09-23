package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ParametroFeriasResponseDTO {
    private String id;
    private Integer vigenteDesde;
    private String prazoPreferencia;
    private String prazoMapa;
    private String fixacaoInicio;
    private String fixacaoFim;
    private Integer periodoMinimoInterpolado;
    private String fundamento;
    /** TABELA (linha de t_parametro_ferias) ou LEI (sem linha em vigor: os valores da lei, sem id). */
    private String origem;
}
