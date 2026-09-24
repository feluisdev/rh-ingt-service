package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Uma fatia de um indicador: a categoria (ex.: «F», «30-39», «Técnica») e quantos. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ContagemDTO {
    private String chave;
    private int valor;
}
