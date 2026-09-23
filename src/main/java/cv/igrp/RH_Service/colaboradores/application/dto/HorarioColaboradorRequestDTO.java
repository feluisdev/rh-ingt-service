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
public class HorarioColaboradorRequestDTO {
    private String horarioId;
    /** PRESENCIAL, TELETRABALHO ou MISTO (art. 166.º); omisso = PRESENCIAL. */
    private String regimePrestacao;
    private LocalDate dataInicio;
}
