package cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity;

import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CareerEntity;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CategoryEntity;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_organica_id", nullable = false)
    private OrganizationalUnitEntity unidadeOrganica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id")
    private CareerEntity career;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_position_id")
    private PositionEntity parentPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manages_unit_id")
    private OrganizationalUnitEntity managesUnit;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "legal_base", length = 255)
    private String legalBase;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
