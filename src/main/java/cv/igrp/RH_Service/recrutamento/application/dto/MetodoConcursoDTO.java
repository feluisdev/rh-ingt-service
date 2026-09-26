package cv.igrp.RH_Service.recrutamento.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Um método de selecção do concurso. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class MetodoConcursoDTO {
    /** TRIAGEM_CURRICULAR, PROVA_CONHECIMENTOS, AVALIACAO_COMPETENCIAS, ENTREVISTA, CURSO_FORMACAO, PROVAS_FISICAS */
    private String metodo;
    /** Em %; os métodos somam 100. */
    private Integer ponderacao;
    private Boolean eliminatorio;
    /** 0 a 20. */
    private BigDecimal notaMinima;
}
