package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TipoDocumentoEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface TipoDocumentoEntityRepository extends
    JpaRepository<TipoDocumentoEntity, UUID>,
    JpaSpecificationExecutor<TipoDocumentoEntity>
{
  Optional<TipoDocumentoEntity> findById(UUID externalId);

  List<TipoDocumentoEntity> findAllByEstado(Estado estado);

}
