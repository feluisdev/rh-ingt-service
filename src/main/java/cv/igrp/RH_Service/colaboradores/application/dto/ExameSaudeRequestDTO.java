package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Registar um exame. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ExameSaudeRequestDTO {
    private String tipo;
    private LocalDate data;
    private String entidade;
    private String resultado;
    /** Obrigatórias no apto condicionado. */
    private String restricoes;
    /** Por omissão: 2 anos (1 a partir dos 50 anos de idade); obrigatória no inapto temporário (reavaliação). */
    private LocalDate validadeAte;
    private String observacoes;
}
