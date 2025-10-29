package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DepartamentoEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipFile;


@Repository
public interface DepartamentoEntityRepository extends
    JpaRepository<DepartamentoEntity, Integer>,
    JpaSpecificationExecutor<DepartamentoEntity>
{

  Optional<DepartamentoEntity> findById(UUID externalId);

  List<DepartamentoEntity> findAllByEstado(Estado estado);

  List<DepartamentoEntity> findAllByResponsavelId_IdAndEstado(UUID responsavelIdExternalId, Estado estado);

  boolean existsById(UUID id);
}
