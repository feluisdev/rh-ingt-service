package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DependenteEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.QualificacaoEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface QualificacaoEntityRepository extends
    JpaRepository<QualificacaoEntity, Integer>,
    JpaSpecificationExecutor<QualificacaoEntity>
{

  Optional<QualificacaoEntity> findById(UUID externalId);

  List<QualificacaoEntity> findAllByEstado(Estado estado);

  List<QualificacaoEntity> findAllByIdFuncionario_Id_AndEstado(UUID idFuncionarioExternalId, Estado estado);
}
