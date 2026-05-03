package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FormacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsFormacaoEntityRepository extends JpaRepository<FormacaoEntity, UUID> {

    List<FormacaoEntity> findAllByFuncionarioId(UUID funcionarioId);

    @Query("SELECT f FROM ColabsFormacaoEntity f WHERE f.funcionarioId = :funcionarioId AND YEAR(f.startDate) = :year")
    List<FormacaoEntity> findAllByFuncionarioIdAndYear(@Param("funcionarioId") UUID funcionarioId, @Param("year") int year);
}
