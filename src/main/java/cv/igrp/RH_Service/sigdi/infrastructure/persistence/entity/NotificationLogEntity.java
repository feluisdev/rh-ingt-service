/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.envers.Audited;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_notification_logs")
public class NotificationLogEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotBlank(message = "recipient is mandatory")
    @Column(name="recipient", nullable = false)
    private String recipient;

  
    @Column(name="channel")
    private String channel;

  
    @Column(name="subject")
    private String subject;

  
    @Column(name="sent_at")
    private String sentAt;

  
    @Column(name="status")
    private String status;

  
}