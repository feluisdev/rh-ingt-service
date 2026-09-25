package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Apresentar, decidir ou recorrer de uma reclamação. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ReclamacaoAntiguidadeRequestDTO {
    /** Apresentar pelo RH (o próprio usa /me). */
    private String funcionarioId;
    /** OMISSAO, GRADUACAO, SITUACAO, CONTAGEM */
    private String fundamento;
    /** Apresentar e recorrer. */
    private String texto;
    /** Presta serviço no estrangeiro (prazos de 60 dias). */
    private Boolean noEstrangeiro;
    /** Decidir. */
    private Boolean deferida;
    /** Decidir o recurso. */
    private Boolean provido;
    /** Decidir e decidir o recurso: a fundamentação. */
    private String decisao;
}
