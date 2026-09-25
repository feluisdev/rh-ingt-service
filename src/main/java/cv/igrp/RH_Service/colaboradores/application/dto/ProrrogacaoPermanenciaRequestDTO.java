package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Pedir ou decidir a permanência para além dos 65 anos. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ProrrogacaoPermanenciaRequestDTO {
    /** Pedir: a vontade do funcionário (obrigatória). */
    private Boolean manifestacaoVontade;
    /** Pedir. */
    private String propostaFundamentada;
    /** Pedir: até quando (no máximo, o dia dos 70 anos). */
    private LocalDate validaAte;
    /** Autorizar. */
    private String despachoNumero;
    /** Autorizar. */
    private LocalDate despachoData;
    /** Indeferir. */
    private String motivo;
}
