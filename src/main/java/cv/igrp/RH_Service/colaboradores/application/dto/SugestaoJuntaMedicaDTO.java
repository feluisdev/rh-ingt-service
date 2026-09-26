package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Colaborador com doença seguida de 30 dias ou mais sem junta pedida (DL n.º 3/2010, art. 26.º; BR-SST-19) */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class SugestaoJuntaMedicaDTO {
    private String funcionarioId;
    private String nome;
    /** Início da doença seguida */
    private LocalDate desde;
    /** Fim do último atestado aprovado */
    private LocalDate ate;
    /** Dias seguidos de doença até à data da consulta */
    private int dias;
}
