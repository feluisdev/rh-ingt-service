package cv.igrp.RH_Service.recrutamento.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Criar ou editar um concurso (rascunho), homologar ou anular. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ConcursoRequestDTO {
    private String referencia;
    private String finalidade;
    private String tipo;
    private String modalidade;
    private String vinculo;
    private String categoriaId;
    private String requisitos;
    private String habilitacaoMinima;
    private Integer quotaDeficiencia;
    private List<String> lugares = new ArrayList<>();
    private List<MetodoConcursoDTO> metodos = new ArrayList<>();
    private String dispensaMetodosDespacho;
    private List<MembroJuriDTO> juri = new ArrayList<>();
    private LocalDate dataAviso;
    private LocalDate candidaturasDe;
    private LocalDate candidaturasAte;
    /** Homologar. */
    private String despacho;
    /** Homologar. */
    private LocalDate data;
    /** Anular. */
    private String motivo;
}
