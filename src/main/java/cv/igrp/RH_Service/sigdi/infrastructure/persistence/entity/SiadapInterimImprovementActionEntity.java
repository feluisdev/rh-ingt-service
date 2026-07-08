package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import org.hibernate.envers.Audited;

/**
 * Entidade JPA para armazenar as ações de melhoria acordadas no feedback intercalar.
 */
@Audited
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_siadap_interim_actions")
public class SiadapInterimImprovementActionEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "evaluation_id", nullable = false)
    private UUID evaluationId;

    @Column(name = "action_agreed", columnDefinition = "TEXT")
    private String actionAgreed;

    @Column(name = "responsible", length = 200)
    private String responsible;

    @Column(name = "deadline", length = 100)
    private String deadline;

    @Column(name = "needed_support", columnDefinition = "TEXT")
    private String neededSupport;
}
