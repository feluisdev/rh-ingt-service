package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface DocumentoEntityRepository extends
    JpaRepository<DocumentoEntity, UUID>,
    JpaSpecificationExecutor<DocumentoEntity>
{
  Optional<DocumentoEntity> findById(UUID externalId);

  List<DocumentoEntity> findByObjectIdAndObjectoTipo(UUID objectId, ObjetoTipo objectoTipo);

  List<DocumentoEntity> findByObjectId(UUID objectId);

  Optional<DocumentoEntity> findFirstByObjectIdAndObjectoTipo(UUID objectId, ObjetoTipo objectoTipo);


}
