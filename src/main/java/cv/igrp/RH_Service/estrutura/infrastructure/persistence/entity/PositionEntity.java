package cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_position")
public class PositionEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "numero_lugar", unique = true, nullable = false, length = 60)
    private String numeroLugar;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "unidade_organica_id", nullable = false)
    private UUID unidadeOrganicaId;

    @Column(name = "career_id")
    private UUID careerId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "parent_position_id")
    private UUID parentPositionId;

    @Column(name = "manages_unit_id")
    private UUID managesUnitId;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "legal_base", length = 255)
    private String legalBase;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
