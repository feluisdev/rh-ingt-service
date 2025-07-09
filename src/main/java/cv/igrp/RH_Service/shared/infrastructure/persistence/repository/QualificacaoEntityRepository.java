package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.QualificacaoEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;



@Repository
public interface QualificacaoEntityRepository extends
    JpaRepository<QualificacaoEntity, Integer>,
    JpaSpecificationExecutor<QualificacaoEntity>
{

}