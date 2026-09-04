package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corpo do pedido de atribuição/correção da menção de mérito de uma avaliação SIADAP
 * (ACH-A-04 / SIA-01).
 * <p>
 * O campo {@code meritRating} transporta o <b>código</b> do enum
 * {@link cv.igrp.RH_Service.sigdi.application.constants.SiadapMeritRating}:
 * {@code INADEQUATE}, {@code REGULAR}, {@code GOOD}, {@code VERY_GOOD} ou {@code EXCELLENT}.
 * <p>
 * A obrigatoriedade do campo é imposta aqui por Jakarta Bean Validation; a validação do
 * <i>valor</i> (código pertencente ao enum) é feita no handler, para que a mensagem de erro
 * possa listar os valores permitidos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AssignMeritRatingRequestDTO {

    @NotBlank(message = "A menção de mérito é obrigatória")
    private String meritRating;
}
