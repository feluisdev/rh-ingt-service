package cv.igrp.RH_Service.formacao.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Uma necessidade de formação do plano. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class NecessidadeFormacaoDTO {
    private String id;
    private String tema;
    private String funcionarioId;
    private String funcionarioNome;
    /** PROPRIO, CHEFIA, RH */
    private String origem;
    /** ALTA, MEDIA, BAIXA */
    private String prioridade;
    private String justificacao;
    /** IDENTIFICADA, PLANEADA, SATISFEITA */
    private String estado;
    private String accaoId;
}
