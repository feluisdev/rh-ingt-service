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
@Entity(name = "ColabsQualificacaoEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_qualificacao")
public class QualificacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "level", length = 50)
    private String level;

    @Column(name = "course_name", length = 200)
    private String courseName;

    @Column(name = "institution", length = 200)
    private String institution;

    @Column(name = "country", length = 10)
    private String country;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "completed")
    private Boolean completed;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
