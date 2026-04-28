package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.OkrEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface OkrEntityRepository extends
    JpaRepository<OkrEntity, UUID>,
    JpaSpecificationExecutor<OkrEntity>
{

      List<OkrEntity> findAllByInstitutionId(UUID institutionId);

      List<OkrEntity> findAllByInstitutionIdAndCycle(UUID institutionId, String cycle);

      default OkrEntity findByIdOrThrow(UUID id) {
          return this.findById(id)
          .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,"OkrEntity not found for id: " + id));
      }

}