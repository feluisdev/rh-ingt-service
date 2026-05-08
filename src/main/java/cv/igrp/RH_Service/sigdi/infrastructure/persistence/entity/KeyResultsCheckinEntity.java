/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.Audited;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_key_result_checkins")
public class KeyResultsCheckinEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "valueAdded is mandatory")
    @Column(name="value_added", nullable = false)
    private BigDecimal valueAdded;

  
    @Column(name="evidence_url")
    private String evidenceUrl;

  
    @Lob
    @Column(name="comment", columnDefinition="TEXT")
    private String comment;

  
    @Column(name="checkin_date")
    private LocalDateTime checkinDate;

  
    @Column(name="institution_id")
    private UUID institutionId;

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "key_result_id")
   private KeyResultsEntity keyResultId;


}