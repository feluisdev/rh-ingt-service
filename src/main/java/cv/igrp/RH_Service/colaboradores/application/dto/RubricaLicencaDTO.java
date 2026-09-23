package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Uma licença ou mobilidade no mês, na relação mensal. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class RubricaLicencaDTO {
    private String codigo;
    private String nome;
    /** LICENCA ou MOBILIDADE. */
    private String tipoRegisto;
    /** Dias de calendário no mês (art. 76.º). */
    private int dias;
    private Boolean afectaRemuneracao;
    private Boolean contaAntiguidade;
}
