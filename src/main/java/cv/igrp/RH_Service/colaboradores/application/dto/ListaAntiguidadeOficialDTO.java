package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** A lista de antiguidade oficial de um serviço num ano, congelada na aprovação. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ListaAntiguidadeOficialDTO {
    private String id;
    private int ano;
    private LocalDate referencia;
    private String unidadeId;
    private boolean incluirSubunidades;
    /** APROVADA, AFIXADA, DEFINITIVA, PUBLICADA, ANULADA */
    private String estado;
    private String aprovadaPor;
    private LocalDate dataAprovacao;
    private LocalDate dataAfixacao;
    private String localAfixacao;
    /** 30 dias depois da afixação. */
    private LocalDate fimPrazoReclamacao;
    /** 60 dias (art. 74.º). */
    private LocalDate fimPrazoReclamacaoEstrangeiro;
    private LocalDate dataDefinitiva;
    private String publicacaoSerie;
    private String publicacaoNumero;
    private LocalDate publicacaoData;
    /** Publicada depois de 30 de Abril. */
    private boolean foraDoPrazoPublicacao;
    private String motivoAnulacao;
    private int versao;
    private int totalLinhas;
    private List<LinhaListaAntiguidadeDTO> linhas = new ArrayList<>();
    private List<ReclamacaoAntiguidadeDTO> reclamacoes = new ArrayList<>();
    private List<String> alertas = new ArrayList<>();
}
