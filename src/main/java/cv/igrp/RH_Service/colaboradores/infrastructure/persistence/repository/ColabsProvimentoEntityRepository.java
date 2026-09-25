package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProvimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsProvimentoEntityRepository extends JpaRepository<ProvimentoEntity, UUID> {

    @Query("""
            SELECT p FROM ColabsProvimentoEntity p
            WHERE p.funcionario.id = :funcionarioId
            ORDER BY p.dataPosse DESC, p.createdDate DESC""")
    List<ProvimentoEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
