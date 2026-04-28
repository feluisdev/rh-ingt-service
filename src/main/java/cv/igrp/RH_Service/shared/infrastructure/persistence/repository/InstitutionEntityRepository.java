package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface InstitutionEntityRepository extends
    JpaRepository<InstitutionEntity, UUID>,
    JpaSpecificationExecutor<InstitutionEntity>
{

      Optional<InstitutionEntity> findByCode(String code);

      default InstitutionEntity findByIdOrThrow(UUID id) {
          return this.findById(id)
          .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,"InstitutionEntity not found for id: " + id));
      }

}