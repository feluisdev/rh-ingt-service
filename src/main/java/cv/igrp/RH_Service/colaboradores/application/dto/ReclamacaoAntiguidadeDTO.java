package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Uma reclamação da lista de antiguidade (arts. 72.º–74.º). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ReclamacaoAntiguidadeDTO {
    private String id;
    private String listaId;
    private String funcionarioId;
    private String nome;
    /** OMISSAO, GRADUACAO, SITUACAO, CONTAGEM */
    private String fundamento;
    private String texto;
    private boolean noEstrangeiro;
    private boolean peloProprio;
    private LocalDate dataApresentacao;
    /** APRESENTADA, DEFERIDA, INDEFERIDA */
    private String estado;
    private String decisao;
    private LocalDate dataDecisao;
    private LocalDate recursoEm;
    private String recursoTexto;
    /** PROVIDO, NAO_PROVIDO */
    private String recursoResultado;
    private String recursoDecisao;
    private LocalDate recursoDecididoEm;
    private List<String> alertas = new ArrayList<>();
}
