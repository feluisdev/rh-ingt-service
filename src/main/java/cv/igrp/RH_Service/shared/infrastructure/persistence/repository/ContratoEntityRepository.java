package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.ContratoEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ContratoEntityRepository extends
    JpaRepository<ContratoEntity, Integer>,
    JpaSpecificationExecutor<ContratoEntity>
{

  Optional<ContratoEntity> findByExternalId(UUID externalId);

  List<ContratoEntity> findByIdFuncionario_ExternalId(UUID funcionarioExternalId);

  Optional<ContratoEntity> findAllByEstado(Estado estado);
}
