package cv.igrp.RH_Service.formacao.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** O plano anual de formação. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PlanoFormacaoDTO {
    private String id;
    private int ano;
    private String unidadeId;
    private String designacao;
    /** RASCUNHO, APROVADO */
    private String estado;
    private String despacho;
    private LocalDate dataAprovacao;
    private List<NecessidadeFormacaoDTO> necessidades = new ArrayList<>();
}
