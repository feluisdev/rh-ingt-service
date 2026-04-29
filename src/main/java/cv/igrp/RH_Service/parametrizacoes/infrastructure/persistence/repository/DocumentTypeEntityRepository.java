package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.DocumentTypeEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentTypeEntityRepository extends
    JpaRepository<DocumentTypeEntity, UUID>,
    JpaSpecificationExecutor<DocumentTypeEntity>
{
    Optional<DocumentTypeEntity> findByCodigo(String codigo);

    java.util.List<DocumentTypeEntity> findAllByIsActive(Boolean isActive);
}
