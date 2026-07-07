package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Entidade JPA que persiste as competências avaliadas de uma avaliação SIADAP.
 * Tabela: t_evaluation_competencies
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_evaluation_competencies")
public class CompetencyItemEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    /** FK para t_siadap_evaluations */
    @Column(name = "evaluation_id", nullable = false)
    private UUID evaluationId;

    /**
     * Código da competência no catálogo da instituição.
     * Ex: "ORIENT_SERV_PUBLICO", "TOMADA_DECISAO", "COMUNICACAO"
     */
    @Column(name = "competency_code", nullable = false, length = 100)
    private String competencyCode;

    /** Nome descritivo da competência (para display). */
    @Column(name = "competency_name", nullable = false, length = 200)
    private String competencyName;

    /**
     * Categoria: BEHAVIORAL | ORGANIZATIONAL | TECHNICAL | LEADERSHIP.
     * Alinhado com o ReCAP (Referencial de Competências para a AP).
     */
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /**
     * Pontuação atribuída (1-5):
     * 1 = Insuficiente, 2 = Regular, 3 = Bom, 4 = Muito Bom, 5 = Excelente.
     * NULL enquanto não avaliado.
     */
    @Column(name = "score")
    private Integer score;
}
