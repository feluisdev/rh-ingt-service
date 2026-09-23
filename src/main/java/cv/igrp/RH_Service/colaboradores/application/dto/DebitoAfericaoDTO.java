package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** O débito de um período de aferição de um horário flexível (DL n.º 3/2010, art. 13.º n.º 2). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class DebitoAfericaoDTO {
    private String horarioNome;
    /** SEMANA ou MES. */
    private String periodoAfericao;
    private LocalDate inicio;
    private LocalDate fim;
    private int minutosEsperados;
    private int minutosTrabalhados;
    /** Já contado nos dias (sem registo, plataformas): não conta outra vez. */
    private int minutosJaEmFalta;
    private int minutosDebito;
}
