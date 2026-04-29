package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.KeyResultsCheckinEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResultCheckin;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultCheckinId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import org.springframework.stereotype.Component;

@Component
public class KeyResultCheckinMapper {

  public KeyResultCheckin toDomain(KeyResultsCheckinEntity entity) {
    if (entity == null) return null;

    return KeyResultCheckin.reconstruct(
        KeyResultCheckinId.from(entity.getId()),
        KeyResultId.from(entity.getKeyResultId().getId()),
        entity.getValueAdded(),
        entity.getEvidenceUrl(),
        entity.getComment(),
        entity.getCheckinDate()
    );
  }

  public KeyResultsCheckinEntity toEntity(KeyResultCheckin domain) {
    if (domain == null) return null;

    KeyResultsCheckinEntity entity = new KeyResultsCheckinEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setValueAdded(domain.getValueAdded());
    entity.setEvidenceUrl(domain.getEvidenceUrl());
    entity.setComment(domain.getComment());
    entity.setCheckinDate(domain.getCheckinDate());

    // Referência leve — só o ID
    KeyResultsEntity keyResultRef = new KeyResultsEntity();
    keyResultRef.setId(domain.getKeyResultId().getValor().getValor());
    entity.setKeyResultId(keyResultRef);

    return entity;
  }

}
