package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Um estágio probatório ou período experimental. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PeriodoProvaDTO {
    private String id;
    private String provimentoId;
    private String funcionarioId;
    private String nome;
    /** ESTAGIO_PROBATORIO ou PERIODO_EXPERIMENTAL */
    private String tipo;
    private LocalDate inicio;
    private LocalDate fimPrevisto;
    private String tutorId;
    private String tutorNome;
    /** EM_CURSO, CONCLUIDO_COM_SUCESSO, CONCLUIDO_SEM_SUCESSO, CESSADO_ANTECIPADAMENTE, DENUNCIADO */
    private String estado;
    private LocalDate dataRelatorio;
    /** POSITIVA ou NEGATIVA */
    private String avaliacao;
    private String fundamentacao;
    private LocalDate dataFim;
    private List<String> alertas = new ArrayList<>();
}
