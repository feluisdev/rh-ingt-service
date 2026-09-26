package cv.igrp.RH_Service.recrutamento.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Um procedimento concursal, com as candidaturas. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ConcursoDTO {
    private String id;
    private String referencia;
    /** INGRESSO ou ACESSO */
    private String finalidade;
    /** COMUM ou ESPECIAL */
    private String tipo;
    /** EXTERNO, INTERNO, INTERNO_RESTRITO */
    private String modalidade;
    /** A forma de vínculo a constituir (ModalidadeProvimento). */
    private String vinculo;
    private String categoriaId;
    private List<String> lugares = new ArrayList<>();
    private String requisitos;
    private String habilitacaoMinima;
    private Integer quotaDeficiencia;
    private List<MetodoConcursoDTO> metodos = new ArrayList<>();
    private String dispensaMetodosDespacho;
    private List<MembroJuriDTO> juri = new ArrayList<>();
    private LocalDate dataAviso;
    private LocalDate candidaturasDe;
    private LocalDate candidaturasAte;
    /** RASCUNHO, ABERTO, CANDIDATURAS_ENCERRADAS, EM_AVALIACAO, LISTA_PROVISORIA, HOMOLOGADO, CONCLUIDO, ANULADO */
    private String estado;
    private String homologacaoDespacho;
    private LocalDate homologacaoData;
    /** Até quando a lista vale como reserva de recrutamento. */
    private LocalDate reservaAte;
    private String motivoAnulacao;
    private int totalCandidaturas;
    private List<CandidaturaDTO> candidaturas = new ArrayList<>();
}
