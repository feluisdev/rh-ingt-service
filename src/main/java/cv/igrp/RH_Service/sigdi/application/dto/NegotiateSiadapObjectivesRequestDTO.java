package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corpo do pedido de negociação de objetivos SIADAP.
 * <p>
 * Ao contrário de {@link NegotiateActivityDTO} (PAA), o comentário é OPCIONAL:
 * o avaliado pode solicitar negociação sem obrigatoriamente justificar por escrito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class NegotiateSiadapObjectivesRequestDTO {

    private String comment;
}
