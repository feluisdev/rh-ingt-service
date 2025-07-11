package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import aj.org.objectweb.asm.commons.Remapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DependenteEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface DependenteEntityRepository extends
    JpaRepository<DependenteEntity, Integer>,
    JpaSpecificationExecutor<DependenteEntity>
{

  Optional<DependenteEntity> findByExternalId(UUID externalId);

  List<DependenteEntity> findAllByEstado(Estado estado);

  List<DependenteEntity> findAllByIdFuncionario_ExternalId_AndEstado(UUID idFuncionarioExternalId, Estado estado);
}
