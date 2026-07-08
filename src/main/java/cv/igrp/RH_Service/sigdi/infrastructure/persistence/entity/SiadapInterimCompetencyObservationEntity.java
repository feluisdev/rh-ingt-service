package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import org.hibernate.envers.Audited;

/**
 * Entidade JPA para armazenar as observações de competências comportamentais na ficha intercalar.
 */
@Audited
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_siadap_interim_competency_observations")
public class SiadapInterimCompetencyObservationEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "evaluation_id", nullable = false)
    private UUID evaluationId;

    @Column(name = "competency_code", nullable = false, length = 100)
    private String competencyCode;

    @Column(name = "observed_evidence", columnDefinition = "TEXT")
    private String observedEvidence;
}
