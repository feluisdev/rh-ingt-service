package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um período de férias. {@code diasUteis} só vem preenchido na marcação. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodoFeriasDTO {
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Integer diasUteis;
}
