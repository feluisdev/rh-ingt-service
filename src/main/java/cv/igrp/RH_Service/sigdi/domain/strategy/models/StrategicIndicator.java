package cv.igrp.RH_Service.sigdi.domain.strategy.models;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class StrategicIndicator {

    private final UUID id;
    private final String title;
    private final String formula;
    private final BigDecimal target;
    private final String evaluationCriteria;
    private final String infoSource;
    private final BigDecimal weight;
    private final String criteriaSuperado;
    private final String criteriaSeguranca;
    private final String criteriaAlcancado;
    private final String criteriaInsuficiente;

    private StrategicIndicator(UUID id, String title, String formula, BigDecimal target,
                               String evaluationCriteria, String infoSource, BigDecimal weight,
                               String criteriaSuperado, String criteriaSeguranca,
                               String criteriaAlcancado, String criteriaInsuficiente) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Indicator title is required");
        }
        this.id = id;
        this.title = title;
        this.formula = formula;
        this.target = target;
        this.evaluationCriteria = evaluationCriteria;
        this.infoSource = infoSource;
        this.weight = weight != null ? weight : BigDecimal.valueOf(1.0);
        this.criteriaSuperado = criteriaSuperado;
        this.criteriaSeguranca = criteriaSeguranca;
        this.criteriaAlcancado = criteriaAlcancado;
        this.criteriaInsuficiente = criteriaInsuficiente;
    }

    public static StrategicIndicator create(String title, String formula, BigDecimal target,
                                            String evaluationCriteria, String infoSource, BigDecimal weight,
                                            String criteriaSuperado, String criteriaSeguranca,
                                            String criteriaAlcancado, String criteriaInsuficiente) {
        return new StrategicIndicator(UUID.randomUUID(), title, formula, target, evaluationCriteria, infoSource, weight,
                criteriaSuperado, criteriaSeguranca, criteriaAlcancado, criteriaInsuficiente);
    }

    public static StrategicIndicator reconstruct(UUID id, String title, String formula, BigDecimal target,
                                                 String evaluationCriteria, String infoSource, BigDecimal weight,
                                                 String criteriaSuperado, String criteriaSeguranca,
                                                 String criteriaAlcancado, String criteriaInsuficiente) {
        return new StrategicIndicator(id, title, formula, target, evaluationCriteria, infoSource, weight,
                criteriaSuperado, criteriaSeguranca, criteriaAlcancado, criteriaInsuficiente);
    }
}
