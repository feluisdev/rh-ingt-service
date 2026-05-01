package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

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
@Entity(name = "ColabsTipoAusenciaEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_leave_type")
public class TipoAusenciaEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "description")
    private String description;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "deducts_balance", nullable = false)
    private Boolean deductsBalance;

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval;

    @Column(name = "max_days_per_year")
    private Integer maxDaysPerYear;

    @Column(name = "category_option_id")
    private UUID categoryOptionId;

    @Column(name = "is_active")
    private Boolean isActive;
}
