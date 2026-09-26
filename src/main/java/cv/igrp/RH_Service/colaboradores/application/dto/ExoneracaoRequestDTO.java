package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Pedir ou deferir. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ExoneracaoRequestDTO {
    /** Pedir: por omissão, hoje. */
    private LocalDate dataPreAviso;
    /** Pedir: por omissão, o pré-aviso + 60 dias. */
    private LocalDate dataPretendida;
    /** Pedir. */
    private String motivo;
    /** Deferir. */
    private String despacho;
    /** Deferir: a data do despacho. */
    private LocalDate data;
}
