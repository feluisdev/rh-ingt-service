package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade JPA que persiste os objetivos individuais de uma avaliação SIADAP.
 * Tabela: t_evaluation_objectives
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_evaluation_objectives")
public class IndividualObjectiveEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    /** FK para t_siadap_evaluations */
    @Column(name = "evaluation_id", nullable = false)
    private UUID evaluationId;

    /** Código único do objetivo dentro da avaliação (ex: "OBJ_1", "OBJ_2"). */
    @Column(name = "objective_code", nullable = false, length = 50)
    private String objectiveCode;

    /** Descrição do resultado esperado. */
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    /** Indicador de medição (KPI). Ex: "% de dossiers concluídos". */
    @Column(name = "indicator", length = 200)
    private String indicator;

    /** Meta definida na contratualização. */
    @Column(name = "target_value", precision = 15, scale = 4)
    private BigDecimal targetValue;

    /** Valor atingido (preenchido durante/após avaliação). */
    @Column(name = "achieved_value", precision = 15, scale = 4)
    private BigDecimal achievedValue;

    /**
     * Pontuação SIADAP: 1 (não atingido), 3 (atingido), 5 (superado).
     * NULL enquanto não avaliado.
     */
    @Column(name = "score")
    private Integer score;

    /**
     * Peso deste objetivo nos resultados (em %).
     * A soma dos pesos de todos os objetivos de uma avaliação = 100.
     */
    @Column(name = "weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;
}
