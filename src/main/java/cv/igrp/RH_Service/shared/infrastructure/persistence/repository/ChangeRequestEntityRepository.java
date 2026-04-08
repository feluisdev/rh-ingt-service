package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.ChangeRequestEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ChangeRequestEntityRepository extends
    JpaRepository<ChangeRequestEntity, UUID>,
    JpaSpecificationExecutor<ChangeRequestEntity>
{

      default ChangeRequestEntity findByIdOrThrow(UUID id) {
          return this.findById(id)
          .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,"ChangeRequestEntity not found for id: " + id));
      }

}