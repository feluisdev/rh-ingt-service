package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsProcessoDisciplinarEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_disciplinary_process")
public class ProcessoDisciplinarEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "process_number", length = 100)
    private String processNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "penalty", length = 255)
    private String penalty;

    @Column(name = "penalty_start_date")
    private LocalDate penaltyStartDate;

    @Column(name = "penalty_end_date")
    private LocalDate penaltyEndDate;

    @Column(name = "official_bulletin", length = 255)
    private String officialBulletin;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "document_id")
    private UUID documentId;
}
