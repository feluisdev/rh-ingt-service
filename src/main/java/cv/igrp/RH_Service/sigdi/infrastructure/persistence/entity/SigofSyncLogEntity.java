/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_sigof_sync_log")
public class SigofSyncLogEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "institutionId is mandatory")
    @Column(name="institution_id", nullable = false)
    private UUID institutionId;

  
    @Column(name="sync_started_at")
    private LocalDateTime syncStartedAt;

  
    @Column(name="sync_ended_at")
    private LocalDateTime syncEndedAt;

  
    @Column(name="status")
    private String status;

  
    @Column(name="records_updated")
    private Integer recordsUpdated;

  
    @Lob
    @Column(name="error_message", columnDefinition="TEXT")
    private String errorMessage;

  
    @Column(name="fiscal_year")
    private Integer fiscalYear;

  
}