package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/** Um facto do RH para o processamento salarial. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FactoRhDTO {
    private String id;
    private String funcionarioId;
    private String numeroFuncionario;
    private String nif;
    private String nome;
    /** ADMISSAO, PROGRESSAO, CESSACAO, … (TipoFactoRh). */
    private String tipo;
    private LocalDate dataEfeito;
    /** yyyy-MM: o mês de processamento em que entra. */
    private String mesCompetencia;
    /** A data de efeito é de um mês já fechado: entra como ajuste neste. */
    private boolean ajusteDeMesAnterior;
    private String referenciaTipo;
    private String referenciaId;
    private String descricao;
    /** Contexto do facto (Lugar, escalão, estado…), por ids e códigos. */
    private Map<String, String> dados;
    private LocalDateTime registadoEm;
}
