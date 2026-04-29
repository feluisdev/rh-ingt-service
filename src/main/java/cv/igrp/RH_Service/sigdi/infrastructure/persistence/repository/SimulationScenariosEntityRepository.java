package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SimulationScenariosEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface SimulationScenariosEntityRepository extends
    JpaRepository<SimulationScenariosEntity, UUID>,
    JpaSpecificationExecutor<SimulationScenariosEntity>
{

      default SimulationScenariosEntity findByIdOrThrow(UUID id) {
          return this.findById(id)
          .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,"SimulationScenariosEntity not found for id: " + id));
      }

}