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
@Table(name = "t_contract_type")
public class ContractTypeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "vinculo_laboral_id")
    private UUID vinculoLaboralId;

    @Column(name = "is_renewable", nullable = false, columnDefinition = "boolean default false")
    private Boolean isRenewable;

    @Column(name = "max_renewals")
    private Integer maxRenewals;

    @Column(name = "max_duration_months")
    private Integer maxDurationMonths;

    @Column(name = "requires_career_structure")
    private Boolean requiresCareerStructure;

    @Column(name = "is_active")
    private Boolean isActive;
}
