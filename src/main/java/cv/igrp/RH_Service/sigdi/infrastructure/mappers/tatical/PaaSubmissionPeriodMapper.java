package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.PaaSubmissionPeriodEntity;
import org.springframework.stereotype.Component;

@Component
public class PaaSubmissionPeriodMapper {

    public PaaSubmissionPeriod toDomain(PaaSubmissionPeriodEntity entity) {
        if (entity == null) return null;

        return PaaSubmissionPeriod.reconstruct(
                entity.getId(),
                Purpose.fromCodeOrThrow(entity.getPurpose()),
                PaaLevel.fromCodeOrThrow(entity.getType()),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus(),
                entity.getYear(),
                entity.getCreatedDate(),
                entity.getCreatedBy()
        );
    }

    // toEntity intentionally does NOT write createdDate/createdBy: the only writer of those two
    // columns is AuditingEntityListener over updatable=false columns (created_date/created_by in
    // AuditEntity). Writing them here would aggravate A-132-101, which D-19 expressly forbids.
    public PaaSubmissionPeriodEntity toEntity(PaaSubmissionPeriod domain) {
        if (domain == null) return null;

        PaaSubmissionPeriodEntity entity = new PaaSubmissionPeriodEntity();
        entity.setId(domain.getId());
        entity.setType(domain.getType().getCode());
        entity.setPurpose(domain.getPurpose().getCode());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setStatus(domain.getStatus());
        entity.setYear(domain.getYear());
        return entity;
    }
}
