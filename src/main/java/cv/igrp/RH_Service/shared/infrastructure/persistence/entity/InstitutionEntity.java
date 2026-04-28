/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_institutions")
public class InstitutionEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotBlank(message = "code is mandatory")
    @Column(name="code", unique = true, nullable = false)
    private String code;

  
    @Column(name="name")
    private String name;

  
    @Column(name="type")
    private String type;

  
    @Column(name="is_active")
    private boolean isActive;


    @Column(name="deactivated_at")
    private OffsetDateTime deactivatedAt;


    @Column(name="contact_email", length = 255)
    private String contactEmail;


}