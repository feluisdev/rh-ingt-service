package cv.igrp.RH_Service.sigdi.domain.compliance.valueobject;

import cv.igrp.RH_Service.sigdi.application.constants.CompetencyCategory;
import lombok.Getter;

/**
 * Value Object representando uma competência avaliada no ciclo SIADAP.
 * <p>
 * Cada competência é identificada por um código do catálogo (ex: "COMP_ORIENT_SERV"),
 * pertence a uma categoria (comportamental, organizacional, técnica, liderança)
 * e é avaliada numa escala de 1 a 5.
 */
@Getter
public class CompetencyItem {

    /**
     * Código da competência no catálogo da instituição.
     * Ex: "ORIENT_SERV_PUBLICO", "TOMADA_DECISAO", "COMUNICACAO"
     */
    private final String competencyCode;

    /** Nome descritivo da competência (para display). */
    private final String competencyName;

    /** Categoria a que pertence (BEHAVIORAL, ORGANIZATIONAL, TECHNICAL, LEADERSHIP). */
    private final CompetencyCategory category;

    /**
     * Pontuação atribuída (1-5).
     * 1 = Insuficiente, 2 = Regular, 3 = Bom, 4 = Muito Bom, 5 = Excelente.
     * Null se ainda não avaliado.
     */
    private final Integer score;

    private CompetencyItem(String competencyCode, String competencyName,
                           CompetencyCategory category, Integer score) {
        if (competencyCode == null || competencyCode.isBlank())
            throw new IllegalArgumentException("competencyCode é obrigatório");
        if (competencyName == null || competencyName.isBlank())
            throw new IllegalArgumentException("competencyName é obrigatório");
        if (category == null) throw new IllegalArgumentException("category é obrigatória");
        if (score != null && (score < 1 || score > 5))
            throw new IllegalArgumentException("score deve estar entre 1 e 5");

        this.competencyCode = competencyCode;
        this.competencyName = competencyName;
        this.category = category;
        this.score = score;
    }

    /** Cria uma competência sem pontuação (contratualizada mas não avaliada). */
    public static CompetencyItem create(String code, String name, CompetencyCategory category) {
        return new CompetencyItem(code, name, category, null);
    }

    /** Reconstrói a partir da persistência. */
    public static CompetencyItem reconstruct(String code, String name, CompetencyCategory category, Integer score) {
        return new CompetencyItem(code, name, category, score);
    }

    /** Retorna nova instância com a pontuação atribuída pelo avaliador. */
    public CompetencyItem withScore(int score) {
        return new CompetencyItem(this.competencyCode, this.competencyName, this.category, score);
    }

    public boolean isEvaluated() {
        return score != null;
    }
}
