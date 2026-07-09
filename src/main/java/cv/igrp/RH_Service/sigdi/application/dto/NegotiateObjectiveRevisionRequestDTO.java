package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corpo do pedido de negociação de uma revisão de objetivo (RECONC-02).
 * <p>
 * Espelha {@link NegotiateSiadapObjectivesRequestDTO} mas é a sua própria classe dedicada
 * (convenção "uma DTO por comando" deste código-base): o comentário é OPCIONAL — o avaliado
 * pode solicitar negociação sem obrigatoriamente justificar por escrito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class NegotiateObjectiveRevisionRequestDTO {

    private String comment;
}
