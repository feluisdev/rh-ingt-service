package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface KeyResultsEntityRepository extends
    JpaRepository<KeyResultsEntity, UUID>,
    JpaSpecificationExecutor<KeyResultsEntity>
{

      default KeyResultsEntity findByIdOrThrow(UUID id) {
          return this.findById(id)
          .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,"KeyResultsEntity not found for id: " + id));
      }

      // CR-02 fix (Phase 80): institution-scoped finder so KeyResults queries (e.g. the
      // dashboard's okrsAtRisk count) never aggregate across every institution in the system.
      List<KeyResultsEntity> findAllByInstitutionId(UUID institutionId);

}