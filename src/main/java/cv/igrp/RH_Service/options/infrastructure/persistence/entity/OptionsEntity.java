/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.options.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_options")
public class OptionsEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotBlank(message = "ccode is mandatory")
    @Column(name="ccode", nullable = false)
    private String ccode;

  
    @Column(name="ckey")
    private String ckey;

  
    @Column(name="cvalue")
    private String cvalue;

  
    @Column(name="locale")
    private String locale;

  
    @Column(name="sort_order")
    private Integer sortOrder;

  
    @Column(name="active")
    private boolean active;

  
    @Lob
    @Column(name="metadata", columnDefinition="TEXT")
    private String metadata;

  
    @Lob
    @Column(name="description", columnDefinition="TEXT")
    private String description;

  
}