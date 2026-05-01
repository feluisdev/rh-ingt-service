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
@Entity(name = "ColabsSubtipoLicencaMobilidadeEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_leave_mobility_subtype")
public class SubtipoLicencaMobilidadeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "description")
    private String description;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "record_type", nullable = false, length = 20)
    private String recordType;

    @Column(name = "affects_pay", nullable = false)
    private Boolean affectsPay;

    @Column(name = "counts_for_seniority", nullable = false)
    private Boolean countsForSeniority;

    @Column(name = "can_self_submit", nullable = false)
    private Boolean canSelfSubmit;

    @Column(name = "is_active")
    private Boolean isActive;
}
