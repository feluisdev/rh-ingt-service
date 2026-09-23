package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Semana ISO-8601 (segunda a domingo). Só os dias dentro do período consultado entram nos totais. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class SemanaAssiduidadeDTO {
    private int ano;
    private int semana;
    /** A segunda-feira da semana. */
    private LocalDate inicio;
    private int minutosTrabalhados;
    private int minutosEsperados;
}
