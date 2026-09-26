package cv.igrp.RH_Service.formacao.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Uma inscrição numa acção de formação. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class InscricaoFormacaoDTO {
    private String id;
    private String funcionarioId;
    private String funcionarioNome;
    /** PROPRIO, CHEFIA, RH */
    private String origem;
    /** PEDIDA, ADMITIDA, RECUSADA, DESISTIU, APROVEITAMENTO, SEM_APROVEITAMENTO, FALTOU */
    private String estado;
    private LocalDate data;
    private String motivo;
    private Integer diasPresenca;
    /** Até quando o formando deve permanência (art. 95.º b)). */
    private LocalDate garantiaAte;
}
