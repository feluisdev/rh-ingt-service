/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_user_delegations")
public class UserDelegationEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "institutionId is mandatory")
    @Column(name="institution_id", nullable = false)
    private UUID institutionId;

  
    @Column(name="delegator_id")
    private UUID delegatorId;

  
    @Column(name="delegate_id")
    private UUID delegateId;

  
    @Column(name="scope")
    private String scope;

  
    @Column(name="start_date")
    private LocalDate startDate;

  
    @Column(name="end_date")
    private LocalDate endDate;

  
    @Column(name="is_active")
    private boolean isActive;

  
}