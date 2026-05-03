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
@Entity(name = "ColabsFormacaoEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_training")
public class FormacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "institution", length = 255)
    private String institution;

    @Column(name = "type_option_key", length = 100)
    private String typeOptionKey;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "document_id")
    private UUID documentId;
}
