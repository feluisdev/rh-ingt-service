package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Decisão da chefia sobre um pedido de ausência da equipa. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class DecisaoPedidoAusenciaRequestDTO {
    /** Obrigatório ao rejeitar; opcional ao aprovar (fica nas observações da decisão). */
    private String motivo;
}
