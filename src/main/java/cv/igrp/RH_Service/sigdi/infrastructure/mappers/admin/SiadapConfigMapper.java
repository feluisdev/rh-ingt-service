package cv.igrp.RH_Service.sigdi.infrastructure.mappers.admin;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.sigdi.domain.admin.models.SiadapConfig;
import org.springframework.stereotype.Component;

@Component
public class SiadapConfigMapper {

  public SiadapConfig toDomain(SiadapConfigEntity entity) {
    if (entity == null) return null;

    return SiadapConfig.reconstruct(
        entity.getId(),
        entity.getFiscalYear(),
        entity.getGoodScore(),
        entity.getExcellentScore(),
        entity.getExcellentQuota(),
        entity.getMinCollaboratorsForQuota(),
        entity.getResultsWeight(),
        entity.getCompetenciesWeight()
    );
  }

  public SiadapConfigEntity toEntity(SiadapConfig domain) {
    if (domain == null) return null;

    SiadapConfigEntity entity = new SiadapConfigEntity();
    entity.setId(domain.getId());
    entity.setFiscalYear(domain.getFiscalYear());
    entity.setGoodScore(domain.getGoodScore());
    entity.setExcellentScore(domain.getExcellentScore());
    entity.setExcellentQuota(domain.getExcellentQuota());
    entity.setMinCollaboratorsForQuota(domain.getMinCollaboratorsForQuota());
    entity.setResultsWeight(domain.getResultsWeight());
    entity.setCompetenciesWeight(domain.getCompetenciesWeight());
    return entity;
  }
}
