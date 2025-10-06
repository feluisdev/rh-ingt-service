package cv.igrp.RH_Service.options.infrastructure.persistence.repository;

import cv.igrp.RH_Service.options.infrastructure.persistence.entity.OptionsEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface OptionsEntityRepository extends
    JpaRepository<OptionsEntity, UUID>,
    JpaSpecificationExecutor<OptionsEntity>
{

      default OptionsEntity findByIdOrThrow(UUID id) {
          return this.findById(id)
          .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,"OptionsEntity not found for id: " + id));
      }

}