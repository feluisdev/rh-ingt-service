package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um prazo que corre no processo. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PrazoDisciplinarDTO {
    private String nome;
    private LocalDate data;
    private boolean vencido;
}
