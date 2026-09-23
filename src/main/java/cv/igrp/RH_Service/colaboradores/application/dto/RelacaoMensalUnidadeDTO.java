package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/** Uma unidade orgânica na relação mensal, com as suas linhas. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class RelacaoMensalUnidadeDTO {
    private String unidadeId;
    private String codigo;
    private String nome;
    private int colaboradores;
    private int comPendencias;
    private BigDecimal faltasPorJustificar;
    private List<RelacaoMensalLinhaDTO> linhas;
}
