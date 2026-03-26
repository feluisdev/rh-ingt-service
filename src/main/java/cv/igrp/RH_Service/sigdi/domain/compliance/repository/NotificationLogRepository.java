package cv.igrp.RH_Service.sigdi.domain.compliance.repository;

import cv.igrp.RH_Service.sigdi.domain.compliance.models.NotificationLog;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.NotificationLogId;

import java.util.List;
import java.util.Optional;

public interface NotificationLogRepository {

  NotificationLog save(NotificationLog log);

  Optional<NotificationLog> findById(NotificationLogId id);

  List<NotificationLog> findByRecipient(String recipient);
}

