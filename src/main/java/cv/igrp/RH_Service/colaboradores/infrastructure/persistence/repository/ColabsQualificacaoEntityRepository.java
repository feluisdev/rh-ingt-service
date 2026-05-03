package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.QualificacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ColabsQualificacaoEntityRepository extends JpaRepository<QualificacaoEntity, UUID> {
    List<QualificacaoEntity> findByFuncionarioId(UUID funcionarioId);
}
