/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* EXTENDED WITH CUSTOM FIELDS — DO NOT REVERT WITHOUT UPDATING THE iGRP STUDIO MANIFEST */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

import org.hibernate.envers.Audited;

/**
 * Entidade JPA da avaliação de desempenho SIADAP.
 * Contém os campos base da avaliação. Os objetivos e competências são
 * persistidos em tabelas separadas (t_evaluation_objectives, t_evaluation_competencies).
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_siadap_evaluations")
public class SiadapEvaluationEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @NotBlank(message = "employeeId is mandatory")
    @Column(name="employee_id", nullable = false)
    private String employeeId;

    @Column(name="year")
    private String year;

    /** Unidade orgânica do colaborador avaliado. */
    @Column(name="organic_unit_id")
    private String organicUnitId;

    /** ID do avaliador (gestor direto). */
    @Column(name="evaluator_id")
    private String evaluatorId;

    /**
     * Fase atual do ciclo: OPEN | IN_PROGRESS | SELF_EVALUATION |
     * MANAGER_EVALUATION | HARMONIZATION | CLOSED
     */
    @Column(name="evaluation_phase", length = 30)
    private String evaluationPhase;

    /**
     * Estado de aceitação da proposta de objetivos individuais:
     * PENDING_ACCEPTANCE | ACCEPTED | NEGOTIATING | TACITLY_ACCEPTED
     */
    @Column(name="acceptance_status", length = 30)
    private String acceptanceStatus;

    /**
     * WR-02: comentário/justificação do avaliado no último pedido de negociação dos objetivos
     * propostos. {@code null} se nunca houve negociação — apenas o último comentário é guardado.
     */
    @Lob
    @Column(name="last_negotiation_comment", columnDefinition = "TEXT")
    private String lastNegotiationComment;

    /** Nota de autoavaliação submetida pelo colaborador (1-5). */
    @Column(name="self_evaluation_score", precision = 4, scale = 2)
    private BigDecimal selfEvaluationScore;

    /**
     * Nota final calculada: média ponderada de resultados e competências.
     * @deprecated campo legado — use IndividualObjectiveEntity + CompetencyItemEntity para cálculo rico.
     */
    @Column(name="objectives_score")
    private BigDecimal objectivesScore;

    /**
     * @deprecated campo legado — substituído por avaliação granular de competências.
     */
    @Column(name="competencies_score")
    private BigDecimal competenciesScore;

    @Column(name="final_score")
    private BigDecimal finalScore;

    /** Peso dos resultados (em %) — ex: 60 para 60%. */
    @Column(name="results_weight", precision = 5, scale = 2)
    private BigDecimal resultsWeight;

    /** Peso das competências (em %) — ex: 40 para 40%. */
    @Column(name="competencies_weight", precision = 5, scale = 2)
    private BigDecimal competenciesWeight;

    @Column(name="merit_rating")
    private String meritRating;

    @Column(name="validated_quota")
    private boolean validatedQuota;

}