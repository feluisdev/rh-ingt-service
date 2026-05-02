package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ColabsFuncionarioEntityRepository
        extends JpaRepository<FuncionarioEntity, UUID>, JpaSpecificationExecutor<FuncionarioEntity> {

    boolean existsByNif(String nif);
    boolean existsByNifAndIdNot(String nif, UUID id);
    boolean existsByBiNumero(String biNumero);
    boolean existsByBiNumeroAndIdNot(String biNumero, UUID id);
    Optional<FuncionarioEntity> findByEmailIgnoreCase(String email);
}
