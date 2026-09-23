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
public class HorarioColaboradorResponseDTO {
    private String id;
    private String horarioId;
    private String horarioNome;
    private String regimePrestacao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
}
