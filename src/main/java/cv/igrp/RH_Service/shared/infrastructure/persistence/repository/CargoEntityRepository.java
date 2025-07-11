package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.CargoEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface CargoEntityRepository extends
    JpaRepository<CargoEntity, Integer>,
    JpaSpecificationExecutor<CargoEntity>
{

  Optional<CargoEntity> findByExternalId(UUID externalId);

  List<CargoEntity> findAllByEstado(Estado estado);

}
