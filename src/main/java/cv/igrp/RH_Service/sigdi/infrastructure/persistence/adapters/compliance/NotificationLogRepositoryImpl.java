package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.compliance;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.NotificationLogEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.NotificationLogEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.NotificationLog;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.NotificationLogRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.NotificationLogId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.NotificationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationLogRepositoryImpl implements NotificationLogRepository {

  private final NotificationLogEntityRepository jpaRepository;
  private final NotificationLogMapper mapper;

  @Transactional
  @Override
  public NotificationLog save(NotificationLog log) {
    NotificationLogEntity entity = mapper.toEntity(log);
    NotificationLogEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<NotificationLog> findById(NotificationLogId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<NotificationLog> findByRecipient(String recipient) {
    if (recipient == null || recipient.isBlank()) return List.of();
    return jpaRepository.findByRecipient(recipient).stream()
        .map(mapper::toDomain)
        .toList();
  }
}

