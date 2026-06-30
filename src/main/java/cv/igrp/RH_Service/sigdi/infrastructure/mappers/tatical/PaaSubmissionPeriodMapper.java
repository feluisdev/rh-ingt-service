package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.PaaSubmissionPeriodEntity;
import org.springframework.stereotype.Component;

@Component
public class PaaSubmissionPeriodMapper {

    public PaaSubmissionPeriod toDomain(PaaSubmissionPeriodEntity entity) {
        if (entity == null) return null;

        return PaaSubmissionPeriod.reconstruct(
                entity.getId(),
                PaaLevel.fromCodeOrThrow(entity.getType()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus(),
                entity.getYear()
        );
    }

    public PaaSubmissionPeriodEntity toEntity(PaaSubmissionPeriod domain) {
        if (domain == null) return null;

        PaaSubmissionPeriodEntity entity = new PaaSubmissionPeriodEntity();
        entity.setId(domain.getId());
        entity.setType(domain.getType().getCode());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setStatus(domain.getStatus());
        entity.setYear(domain.getYear());
        return entity;
    }
}
