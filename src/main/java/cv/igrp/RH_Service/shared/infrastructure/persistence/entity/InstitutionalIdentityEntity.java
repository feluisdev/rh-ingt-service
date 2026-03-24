/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.ArrayList;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_institutional_identity")
public class InstitutionalIdentityEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "cycleYear is mandatory")
    @Column(name="cycle_year", nullable = false)
    private Integer cycleYear;

  
    @NotBlank(message = "mission is mandatory")
    @Lob
    @Column(name="mission", nullable = false, columnDefinition="TEXT")
    private String mission;

  
    @NotBlank(message = "vision is mandatory")
    @Lob
    @Column(name="vision", nullable = false, columnDefinition="TEXT")
    private String vision;

  
    @Lob
    @Column(name="values_json", columnDefinition="TEXT")
    private String valuesJson;

  
    @Column(name="version_comment")
    private String versionComment;

  
    @Column(name="is_active")
    private boolean isActive;

  


  @OneToMany(mappedBy = "identityId", fetch = FetchType.LAZY)
private List<StrategicGoalEntity> goals = new ArrayList<>();
}