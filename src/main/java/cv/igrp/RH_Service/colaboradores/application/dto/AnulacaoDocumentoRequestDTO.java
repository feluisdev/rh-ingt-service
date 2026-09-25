package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Anular um documento emitido. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AnulacaoDocumentoRequestDTO {
    private String motivo;
}
