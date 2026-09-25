package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Pedir uma declaração, ou recusá-la. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PedidoDeclaracaoRequestDTO {
    /** VINCULO, TEMPO_SERVICO, ANTIGUIDADE_CATEGORIA, SITUACAO_FUNCIONAL */
    private String tipo;
    /** Para que serve (entra no documento). */
    private String finalidade;
    /** Recusar. */
    private String motivo;
}
