package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import org.hibernate.envers.Audited;

/**
 * Entidade JPA para armazenar os campos principais do Feedback Intercalar.
 */
@Audited
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_siadap_interim_feedback")
public class SiadapInterimFeedbackEntity extends AuditEntity {

    @Id
    @Column(name = "evaluation_id", unique = true, nullable = false)
    private UUID evaluationId;

    @Column(name = "objectives_synthesis", columnDefinition = "TEXT")
    private String objectivesSynthesis;

    @Column(name = "observed_facts_star", columnDefinition = "TEXT")
    private String observedFactsStar;

    @Column(name = "difficulties_obstacles", columnDefinition = "TEXT")
    private String difficultiesObstacles;

    @Column(name = "feedback_and_action", columnDefinition = "TEXT")
    private String feedbackAndAction;
}
