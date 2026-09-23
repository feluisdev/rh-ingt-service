package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class DiaApuradoDTO {
    private LocalDate data;
    /** FUTURO, FORA_DO_VINCULO, ISENTO, FERIADO, AUSENCIA_JUSTIFICADA, LICENCA, MOBILIDADE_EXTERNA, SEM_HORARIO, DESCANSO, POR_CORRIGIR, SEM_FALTA, COM_FALTA. */
    private String estado;
    /** SEM_REGISTO, INCOMPLETO ou PLATAFORMA, quando COM_FALTA. */
    private String motivo;
    private int minutosEsperados;
    private int minutosTrabalhados;
    private int minutosEmFalta;
}
