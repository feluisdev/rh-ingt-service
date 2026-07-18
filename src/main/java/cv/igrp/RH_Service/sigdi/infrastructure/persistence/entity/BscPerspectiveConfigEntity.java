/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_bsc_perspective_config")
public class BscPerspectiveConfigEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;


    @NotBlank(message = "code is mandatory")
    @Column(name = "code", nullable = false, unique = true)
    private String code;


    @NotBlank(message = "label is mandatory")
    @Column(name = "label", nullable = false)
    private String label;


    @NotNull(message = "displayOrder is mandatory")
    @Column(name = "display_order", nullable = false, unique = true)
    private Integer displayOrder;


}
