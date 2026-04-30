/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */
package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity;

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
@Table(name = "t_leave_mobility_subtype")
public class LeaveMobilitySubtypeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "record_type", length = 20)
    private String recordType;

    @Column(name = "affects_pay")
    private Boolean affectsPay;

    @Column(name = "counts_for_seniority")
    private Boolean countsForSeniority;

    @Column(name = "can_self_submit")
    private Boolean canSelfSubmit;

    @Column(name = "is_active")
    private Boolean isActive;
}
