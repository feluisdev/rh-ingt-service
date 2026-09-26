package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Um exame de medicina do trabalho (só a aptidão, sem dados clínicos). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ExameSaudeDTO {
    private String id;
    private String funcionarioId;
    private String funcionarioNome;
    /** ADMISSAO, PERIODICO, OCASIONAL, REGRESSO */
    private String tipo;
    private LocalDate data;
    private String entidade;
    /** APTO, APTO_CONDICIONADO, INAPTO_TEMPORARIO, INAPTO_DEFINITIVO */
    private String resultado;
    private String restricoes;
    private LocalDate validadeAte;
    /** Ainda dentro da validade. */
    private boolean valido;
    private String observacoes;
    private List<String> alertas = new ArrayList<>();
}
