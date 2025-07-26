package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface DocumentoEntityRepository extends
    JpaRepository<DocumentoEntity, Integer>,
    JpaSpecificationExecutor<DocumentoEntity>
{

}