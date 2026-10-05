package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ReservaLugarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsReservaLugarEntityRepository extends JpaRepository<ReservaLugarEntity, UUID> {

    Optional<ReservaLugarEntity> findByFuncionarioActivo(UUID funcionarioId);

    Optional<ReservaLugarEntity> findByLugarActivo(UUID positionId);

    List<ReservaLugarEntity> findByLugarActivoIn(Collection<UUID> positionIds);

    List<ReservaLugarEntity> findByEstadoAndReservadaEmIn(String estado, Collection<LocalDate> dias);
}
