package cv.igrp.RH_Service.sigdi.domain.compliance.valueobject;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Value Object representando um objetivo individual contratualizado e avaliado.
 * <p>
 * No SIADAP, os objetivos individuais são definidos no início do ciclo e avaliados
 * no final, com escala: 1 (não atingido), 3 (atingido), 5 (superado).
 * A ponderação permite que diferentes objetivos tenham pesos distintos no cálculo final.
 */
@Getter
public class IndividualObjective {

    private final String code;
    private final String description;
    /** Indicador de medição (ex: "% de dossiers concluídos", "N.º de relatórios") */
    private final String indicator;
    /** Meta a atingir */
    private final BigDecimal targetValue;
    /** Valor efetivamente atingido (preenchido na avaliação) */
    private final BigDecimal achievedValue;
    /**
     * Resultado SIADAP: 1 (não atingido), 3 (atingido), 5 (superado).
     * Null se ainda não avaliado.
     */
    private final Integer score;
    /**
     * Peso deste objetivo no cálculo dos resultados (em %).
     * A soma dos pesos de todos os objetivos deve ser 100.
     */
    private final BigDecimal weight;

    private IndividualObjective(String code, String description, String indicator,
                                BigDecimal targetValue, BigDecimal achievedValue,
                                Integer score, BigDecimal weight) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code é obrigatório");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("description é obrigatório");
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("weight deve ser positivo");
        if (score != null && score != 1 && score != 3 && score != 5)
            throw new IllegalArgumentException("score deve ser 1, 3 ou 5 (escala SIADAP)");

        this.code = code;
        this.description = description;
        this.indicator = indicator;
        this.targetValue = targetValue;
        this.achievedValue = achievedValue;
        this.score = score;
        this.weight = weight;
    }

    /** Cria um objetivo novo (sem avaliação ainda). */
    public static IndividualObjective create(String code, String description, String indicator,
                                             BigDecimal targetValue, BigDecimal weight) {
        return new IndividualObjective(code, description, indicator, targetValue, null, null, weight);
    }

    /** Reconstrói a partir da persistência. */
    public static IndividualObjective reconstruct(String code, String description, String indicator,
                                                  BigDecimal targetValue, BigDecimal achievedValue,
                                                  Integer score, BigDecimal weight) {
        return new IndividualObjective(code, description, indicator, targetValue, achievedValue, score, weight);
    }

    /**
     * Regista o valor atingido e a classificação SIADAP do objetivo.
     *
     * @param achieved valor efetivamente atingido
     * @param siadapScore 1 (não atingido), 3 (atingido), 5 (superado)
     */
    public IndividualObjective withAchievement(BigDecimal achieved, int siadapScore) {
        return new IndividualObjective(this.code, this.description, this.indicator,
                this.targetValue, achieved, siadapScore, this.weight);
    }

    /** Retorna a pontuação ponderada deste objetivo (score * weight / 100). */
    public BigDecimal getWeightedScore() {
        if (score == null) return BigDecimal.ZERO;
        return new BigDecimal(score).multiply(weight).divide(new BigDecimal("100"), 4,
                java.math.RoundingMode.HALF_UP);
    }

    public boolean isEvaluated() {
        return score != null;
    }
}
