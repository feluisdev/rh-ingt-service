package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.EnquadramentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnquadramentoEntityRepository extends JpaRepository<EnquadramentoEntity, UUID> {
    Optional<EnquadramentoEntity> findByFuncionarioIdAndIsCurrentTrue(UUID funcionarioId);
    List<EnquadramentoEntity> findByFuncionarioIdOrderByDataInicioDesc(UUID funcionarioId);
}
