package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDate;
import org.hibernate.envers.Audited;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_paa_submission_period")
public class PaaSubmissionPeriodEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "type", nullable = false)
    private String type; // UNIT_LEVEL | INDIVIDUAL_LEVEL

    @Column(name = "purpose", nullable = false)
    private String purpose; // PAA | SIADAP

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private String status; // OPEN | CLOSED

    @Column(name = "year", nullable = false)
    private Integer year;
}
