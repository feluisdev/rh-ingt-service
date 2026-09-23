package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Picagem em tempo real pelo próprio (/me): só o sentido; a hora não se escolhe. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PicagemPropriaRequestDTO {
    /** ENTRADA ou SAIDA. A hora é a do servidor. */
    private String sentido;
}
