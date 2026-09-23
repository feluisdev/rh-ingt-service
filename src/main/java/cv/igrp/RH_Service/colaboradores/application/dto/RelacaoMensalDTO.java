package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/** A relação mensal de assiduidade de um serviço (DL n.º 3/2010, art. 75.º). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class RelacaoMensalDTO {
    private String mes;
    /** O mês ainda não acabou. */
    private boolean provisoria;
    private String unidadeId;
    private String unidadeNome;
    private boolean incluirSubunidades;
    private List<RelacaoMensalUnidadeDTO> unidades;
}
