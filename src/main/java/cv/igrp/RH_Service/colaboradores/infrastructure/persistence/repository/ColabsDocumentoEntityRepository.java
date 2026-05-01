package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DocumentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsDocumentoEntityRepository extends JpaRepository<DocumentoEntity, UUID> {

    List<DocumentoEntity> findAllByReferenceEntityAndReferenceId(String referenceEntity, UUID referenceId);

    List<DocumentoEntity> findAllByReferenceEntityAndReferenceIdAndIsActive(
            String referenceEntity, UUID referenceId, Boolean isActive);

    Optional<DocumentoEntity> findByIdAndReferenceEntityAndReferenceId(
            UUID id, String referenceEntity, UUID referenceId);
}
