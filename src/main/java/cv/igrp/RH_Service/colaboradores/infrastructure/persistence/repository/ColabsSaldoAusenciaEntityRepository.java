package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.SaldoAusenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsSaldoAusenciaEntityRepository extends JpaRepository<SaldoAusenciaEntity, UUID> {

    List<SaldoAusenciaEntity> findAllByFuncionario_Id(UUID funcionarioId);

    Optional<SaldoAusenciaEntity> findByFuncionario_IdAndTipoAusencia_IdAndAno(
            UUID funcionarioId, UUID tipoAusenciaId, int ano);

    boolean existsByFuncionario_IdAndTipoAusencia_IdAndAno(
            UUID funcionarioId, UUID tipoAusenciaId, int ano);
}
