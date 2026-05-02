package cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.NotificationLogEntity;
import cv.igrp.RH_Service.sigdi.application.constants.NotificationChannel;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.NotificationLog;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.NotificationLogId;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogMapper {

  public NotificationLog toDomain(NotificationLogEntity entity) {
    if (entity == null) return null;

    return NotificationLog.reconstruct(
        NotificationLogId.from(entity.getId()),
        entity.getRecipient(),
        entity.getChannel() != null ? NotificationChannel.fromCodeOrThrow(entity.getChannel()) : null,
        entity.getSubject(),
        entity.getSentAt(),
        entity.getStatus()
    );
  }

  public NotificationLogEntity toEntity(NotificationLog domain) {
    if (domain == null) return null;

    NotificationLogEntity entity = new NotificationLogEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setRecipient(domain.getRecipient());
    entity.setChannel(domain.getChannel() != null ? domain.getChannel().getCode() : null);
    entity.setSubject(domain.getSubject());
    entity.setSentAt(domain.getSentAt());
    entity.setStatus(domain.getStatus());
    return entity;
  }
}

