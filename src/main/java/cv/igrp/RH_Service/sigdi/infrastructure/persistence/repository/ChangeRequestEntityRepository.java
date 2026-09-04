package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.ChangeRequestEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
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

      // join fetch avoids N+1 on activityId (@ManyToOne, LAZY) when reading the activity title
      // for the workflow inbox projection; explicit countQuery because Spring Data cannot derive
      // a count from a query with join fetch.
      @Query(value = "select cr from ChangeRequestEntity cr join fetch cr.activityId where cr.status = :status",
             countQuery = "select count(cr) from ChangeRequestEntity cr where cr.status = :status")
      Page<ChangeRequestEntity> findPendingWithActivity(@Param("status") String status, Pageable pageable);

      long countByStatus(String status);

}