package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import org.hibernate.envers.Audited;

/**
 * Entidade JPA para armazenar as revisões de objetivos efetuadas na ficha intercalar.
 */
@Audited
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_siadap_interim_objective_revisions")
public class SiadapInterimObjectiveRevisionEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "evaluation_id", nullable = false)
    private UUID evaluationId;

    @Column(name = "current_objective_text", columnDefinition = "TEXT")
    private String currentObjectiveText;

    @Column(name = "revision_justification", columnDefinition = "TEXT")
    private String revisionJustification;

    @Column(name = "new_objective_smart", columnDefinition = "TEXT")
    private String newObjectiveSmart;

    @Column(name = "approval_status", length = 50)
    private String approvalStatus;
}
